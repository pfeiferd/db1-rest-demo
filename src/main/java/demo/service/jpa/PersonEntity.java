package demo.service.jpa;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Entity zur Tabelle person – nicht zu verwechseln mit dem DTO Person. */
@Entity(name = "Person")
@Table(name = "person")
public class PersonEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "first_name", nullable = false, length = 40)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 40)
  private String lastName;

  @Version
  private int version;

  @OneToMany(mappedBy = "person")
  private List<UserEntity> logins = new ArrayList<>();

  protected PersonEntity() {          // für JPA
  }

  public PersonEntity(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public void addLogin(UserEntity u) {
    logins.add(u);
    u.setPerson(this);                // die besitzende Seite
  }

  public Long getId() { return id; }
  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public int getVersion() { return version; }
  public List<UserEntity> getLogins() { return logins; }
}
