package demo.service.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Entity zur Tabelle app_user – nicht zu verwechseln mit dem DTO User. */
@Entity(name = "User")
@Table(name = "app_user")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 40)
  private String login;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Column(nullable = false, length = 10)
  private String role;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_id")
  private PersonEntity person;        // Referenz statt Fremdschlüssel

  protected UserEntity() {            // für JPA
  }

  public UserEntity(String login, String passwordHash, String role) {
    this.login = login;
    this.passwordHash = passwordHash;
    this.role = role;
  }

  public Long getId() { return id; }
  public String getLogin() { return login; }
  public String getRole() { return role; }
  public PersonEntity getPerson() { return person; }
  void setPerson(PersonEntity person) { this.person = person; }
}
