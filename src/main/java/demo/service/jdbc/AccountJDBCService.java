package demo.service.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import demo.service.AccountService;
import demo.service.ConflictException;
import demo.service.NotFoundException;
import demo.service.ServiceException;
import demo.service.dto.Account;
import demo.service.dto.CheckingAccount;
import demo.service.dto.SavingsAccount;

/**
 * Konten mit JDBC. Abbildung der Vererbung: eine Tabelle pro konkreter Klasse
 * (checking_account, savings_account); die gemeinsame Sequenz account_seq hält
 * die ids über beide Tabellen eindeutig.
 */
public class AccountJDBCService implements AccountService {
  // Polymorphes Lesen: beide Tabellen, die Spalte kind sagt, welche Klasse
  private static final String ALL_ACCOUNTS =
      "SELECT 'C' AS kind, id, person_id, balance, version, overdraft_limit,"
      + " NULL AS interest_rate FROM checking_account"
      + " UNION ALL"
      + " SELECT 'S', id, person_id, balance, version, NULL,"
      + " interest_rate FROM savings_account";

  private final DataSource dataSource;

  public AccountJDBCService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Account get(long id) {
    try (Connection c = dataSource.getConnection()) {
      return read(c, id);
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public Account[] getAll() {
    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(ALL_ACCOUNTS + " ORDER BY id");
         ResultSet rs = ps.executeQuery()) {
      List<Account> result = new ArrayList<>();
      while (rs.next()) {
        result.add(fromResultSet(rs));
      }
      return result.toArray(new Account[0]);
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public long create(Account a) {
    try (Connection c = dataSource.getConnection()) {
      long id;
      try (PreparedStatement ps = c.prepareStatement("SELECT NEXT VALUE FOR account_seq");
           ResultSet rs = ps.executeQuery()) {
        rs.next();
        id = rs.getLong(1);
      }
      String sql = a instanceof CheckingAccount
          ? "INSERT INTO checking_account (id, person_id, balance, version, overdraft_limit)"
            + " VALUES (?, ?, ?, 0, ?)"
          : "INSERT INTO savings_account (id, person_id, balance, version, interest_rate)"
            + " VALUES (?, ?, ?, 0, ?)";
      try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, id);
        ps.setLong(2, a.getPersonId());
        ps.setBigDecimal(3, a.getBalance());
        ps.setBigDecimal(4, a instanceof CheckingAccount ca ? ca.getOverdraftLimit()
            : ((SavingsAccount) a).getInterestRate());
        ps.executeUpdate();
      }
      a.setId(id);
      a.setVersion(0);
      return id;
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void update(Account a) {
    String sql = a instanceof CheckingAccount
        ? "UPDATE checking_account SET person_id = ?, balance = ?,"
          + " overdraft_limit = ?, version = version + 1"
          + " WHERE id = ? AND version = ?"
        : "UPDATE savings_account SET person_id = ?, balance = ?,"
          + " interest_rate = ?, version = version + 1"
          + " WHERE id = ? AND version = ?";
    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, a.getPersonId());
      ps.setBigDecimal(2, a.getBalance());
      ps.setBigDecimal(3, a instanceof CheckingAccount ca ? ca.getOverdraftLimit()
          : ((SavingsAccount) a).getInterestRate());
      ps.setLong(4, a.getId());
      ps.setInt(5, a.getVersion());          // die Version aus dem DTO
      if (ps.executeUpdate() == 0) {
        read(c, a.getId());                  // ggf. NotFoundException
        throw new ConflictException(
            "Konto " + a.getId() + " wurde inzwischen geändert");
      }
      a.setVersion(a.getVersion() + 1);
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void remove(long id) {
    try (Connection c = dataSource.getConnection()) {
      Account a = read(c, id);
      try (PreparedStatement ps = c.prepareStatement(
          "DELETE FROM " + table(a) + " WHERE id = ?")) {
        ps.setLong(1, id);
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void transfer(long fromId, long toId, BigDecimal amount) {
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(false);                // ab hier eine Transaktion
      try {
        Account from = read(c, fromId);
        Account to = read(c, toId);
        BigDecimal newBalance = from.getBalance().subtract(amount);
        if (newBalance.compareTo(minBalance(from)) < 0) {
          throw new ServiceException("Deckung reicht nicht");
        }
        writeBalance(c, from, newBalance);                   // 1. UPDATE
        writeBalance(c, to, to.getBalance().add(amount));    // 2. UPDATE
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
  private static BigDecimal minBalance(Account a) {
    return a instanceof CheckingAccount ca ? ca.getOverdraftLimit().negate() : BigDecimal.ZERO;
  }

  private static String table(Account a) {
    return a instanceof CheckingAccount ? "checking_account" : "savings_account";
  }

  private void writeBalance(Connection c, Account a, BigDecimal balance)
      throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("UPDATE " + table(a)
        + " SET balance = ?, version = version + 1"
        + " WHERE id = ? AND version = ?")) {
      ps.setBigDecimal(1, balance);
      ps.setLong(2, a.getId());
      ps.setInt(3, a.getVersion());
      if (ps.executeUpdate() == 0) {
        throw new ConflictException(
            "Konto " + a.getId() + " wurde inzwischen geändert");
      }
    }
  }

  private Account read(Connection c, long id) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement(
        "SELECT * FROM (" + ALL_ACCOUNTS + ") WHERE id = ?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          throw new NotFoundException("Konto " + id + " nicht gefunden");
        }
        return fromResultSet(rs);
      }
    }
  }

  private Account fromResultSet(ResultSet rs) throws SQLException {
    Account a;
    if (rs.getString("kind").equals("C")) {
      CheckingAccount ca = new CheckingAccount();
      ca.setOverdraftLimit(rs.getBigDecimal("overdraft_limit"));
      a = ca;
    } else {
      SavingsAccount sa = new SavingsAccount();
      sa.setInterestRate(rs.getBigDecimal("interest_rate"));
      a = sa;
    }
    a.setId(rs.getLong("id"));
    a.setPersonId(rs.getLong("person_id"));
    a.setBalance(rs.getBigDecimal("balance"));
    a.setVersion(rs.getInt("version"));
    return a;
  }
}
