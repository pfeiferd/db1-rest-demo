package demo.service.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import demo.service.NotFoundException;
import demo.service.PersonService;
import demo.service.ServiceException;
import demo.service.dto.Person;

public class PersonJDBCService implements PersonService {
  private final DataSource dataSource;

  public PersonJDBCService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Person get(long id) {
    String sql = "SELECT id, first_name, last_name FROM person WHERE id = ?";
    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          throw new NotFoundException("Person " + id + " nicht gefunden");
        }
        return fromResultSet(rs);
      }
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public long create(Person p) {
    String sql = "INSERT INTO person (first_name, last_name) VALUES (?, ?)";
    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(sql,
             Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, p.getFirstName());
      ps.setString(2, p.getLastName());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        rs.next();
        p.setId(rs.getLong(1));
        return p.getId();
      }
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public Person[] getAll() {
    String sql = "SELECT id, first_name, last_name FROM person ORDER BY id";
    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<Person> result = new ArrayList<>();
      while (rs.next()) {
        result.add(fromResultSet(rs));
      }
      return result.toArray(new Person[0]);
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void update(Person p) {
    execute("UPDATE person SET first_name = ?, last_name = ? WHERE id = ?",
        p.getFirstName(), p.getLastName(), p.getId());
  }

  @Override
  public void remove(long id) {
    execute("DELETE FROM person WHERE id = ?", id);
  }

  private void execute(String sql, Object... args) {
    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      for (int i = 0; i < args.length; i++) {
        ps.setObject(i + 1, args[i]);
      }
      if (ps.executeUpdate() == 0) {
        throw new NotFoundException("Person nicht gefunden");
      }
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }

  private Person fromResultSet(ResultSet rs) throws SQLException {
    Person p = new Person();
    p.setId(rs.getLong(1));
    p.setFirstName(rs.getString(2));
    p.setLastName(rs.getString(3));
    return p;
  }
}
