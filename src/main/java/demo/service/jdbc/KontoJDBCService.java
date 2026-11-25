package demo.service.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import demo.service.ConflictException;
import demo.service.KontoService;
import demo.service.NotFoundException;
import demo.service.ServiceException;
import demo.service.dto.Girokonto;
import demo.service.dto.Konto;
import demo.service.dto.Sparkonto;

/**
 * Konten mit JDBC. Abbildung der Vererbung: eine Tabelle pro konkreter Klasse
 * (girokonto, sparkonto); die gemeinsame Sequenz konto_seq hält die ids
 * über beide Tabellen eindeutig.
 */
public class KontoJDBCService implements KontoService {
  // Polymorphes Lesen: beide Tabellen, die Spalte art sagt, welche Klasse
  private static final String ALLE_KONTEN =
      "SELECT 'G' AS art, id, person_id, saldo, version, dispo, NULL AS zinssatz"
      + " FROM girokonto"
      + " UNION ALL"
      + " SELECT 'S', id, person_id, saldo, version, NULL, zinssatz"
      + " FROM sparkonto";

  private final DataSource dataSource;

  public KontoJDBCService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Konto get(long id) {
    try (Connection c = dataSource.getConnection()) {
      return lesen(c, id);
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public Konto[] getAll() {
    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(ALLE_KONTEN + " ORDER BY id");
         ResultSet rs = ps.executeQuery()) {
      List<Konto> result = new ArrayList<>();
      while (rs.next()) {
        result.add(fromResultSet(rs));
      }
      return result.toArray(new Konto[0]);
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public long create(Konto k) {
    try (Connection c = dataSource.getConnection()) {
      long id;
      try (PreparedStatement ps = c.prepareStatement("SELECT NEXT VALUE FOR konto_seq");
           ResultSet rs = ps.executeQuery()) {
        rs.next();
        id = rs.getLong(1);
      }
      String sql = k instanceof Girokonto
          ? "INSERT INTO girokonto (id, person_id, saldo, version, dispo) VALUES (?, ?, ?, 0, ?)"
          : "INSERT INTO sparkonto (id, person_id, saldo, version, zinssatz) VALUES (?, ?, ?, 0, ?)";
      try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, id);
        ps.setLong(2, k.getPersonId());
        ps.setBigDecimal(3, k.getSaldo());
        ps.setBigDecimal(4, k instanceof Girokonto g ? g.getDispo()
            : ((Sparkonto) k).getZinssatz());
        ps.executeUpdate();
      }
      k.setId(id);
      k.setVersion(0);
      return id;
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void update(Konto k) {
    String sql = k instanceof Girokonto
        ? "UPDATE girokonto SET person_id = ?, saldo = ?, dispo = ?, version = version + 1"
          + " WHERE id = ? AND version = ?"
        : "UPDATE sparkonto SET person_id = ?, saldo = ?, zinssatz = ?, version = version + 1"
          + " WHERE id = ? AND version = ?";
    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, k.getPersonId());
      ps.setBigDecimal(2, k.getSaldo());
      ps.setBigDecimal(3, k instanceof Girokonto g ? g.getDispo()
          : ((Sparkonto) k).getZinssatz());
      ps.setLong(4, k.getId());
      ps.setInt(5, k.getVersion());          // die Version, die der Client gelesen hat
      if (ps.executeUpdate() == 0) {
        lesen(c, k.getId());                 // NotFoundException, falls es das Konto nicht gibt
        throw new ConflictException("Konto " + k.getId() + " wurde inzwischen geändert");
      }
      k.setVersion(k.getVersion() + 1);
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void remove(long id) {
    try (Connection c = dataSource.getConnection()) {
      Konto k = lesen(c, id);
      try (PreparedStatement ps = c.prepareStatement(
          "DELETE FROM " + tabelle(k) + " WHERE id = ?")) {
        ps.setLong(1, id);
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void ueberweisen(long vonId, long nachId, BigDecimal betrag) {
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(false);                // ab hier eine Transaktion
      try {
        Konto von = lesen(c, vonId);
        Konto nach = lesen(c, nachId);
        BigDecimal neu = von.getSaldo().subtract(betrag);
        if (neu.compareTo(untergrenze(von)) < 0) {
          throw new ServiceException("Deckung reicht nicht");
        }
        saldoSchreiben(c, von, neu);                         // 1. UPDATE
        saldoSchreiben(c, nach, nach.getSaldo().add(betrag)); // 2. UPDATE
        c.commit();                          // beide Änderungen – oder ...
      } catch (SQLException | RuntimeException e) {
        c.rollback();                        // ... keine
        throw e;
      }
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  // Fachlogik im Dienst: Das DTO selbst hat kein Verhalten
  private static BigDecimal untergrenze(Konto k) {
    return k instanceof Girokonto g ? g.getDispo().negate() : BigDecimal.ZERO;
  }

  private static String tabelle(Konto k) {
    return k instanceof Girokonto ? "girokonto" : "sparkonto";
  }

  private void saldoSchreiben(Connection c, Konto k, BigDecimal saldo) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("UPDATE " + tabelle(k)
        + " SET saldo = ?, version = version + 1 WHERE id = ? AND version = ?")) {
      ps.setBigDecimal(1, saldo);
      ps.setLong(2, k.getId());
      ps.setInt(3, k.getVersion());
      if (ps.executeUpdate() == 0) {
        throw new ConflictException("Konto " + k.getId() + " wurde inzwischen geändert");
      }
    }
  }

  private Konto lesen(Connection c, long id) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement(
        "SELECT * FROM (" + ALLE_KONTEN + ") WHERE id = ?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          throw new NotFoundException("Konto " + id + " nicht gefunden");
        }
        return fromResultSet(rs);
      }
    }
  }

  private Konto fromResultSet(ResultSet rs) throws SQLException {
    Konto k;
    if (rs.getString("art").equals("G")) {
      Girokonto g = new Girokonto();
      g.setDispo(rs.getBigDecimal("dispo"));
      k = g;
    } else {
      Sparkonto s = new Sparkonto();
      s.setZinssatz(rs.getBigDecimal("zinssatz"));
      k = s;
    }
    k.setId(rs.getLong("id"));
    k.setPersonId(rs.getLong("person_id"));
    k.setSaldo(rs.getBigDecimal("saldo"));
    k.setVersion(rs.getInt("version"));
    return k;
  }
}
