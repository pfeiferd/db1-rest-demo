package demo.service.jpa;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Version;

/** Abstrakte Oberklasse ohne Tabelle: je eine pro konkreter Klasse. */
@Entity(name = "Account")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AccountEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
  @SequenceGenerator(name = "account_seq", sequenceName = "account_seq",
                     allocationSize = 1)
  private Long id;                    // eindeutig über beide Tabellen

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_id")
  private PersonEntity owner;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal balance;

  @Version
  private int version;

  protected AccountEntity() {         // für JPA
  }

  protected AccountEntity(PersonEntity owner, BigDecimal balance) {
    this.owner = owner;
    this.balance = balance;
  }

  /** Fachlogik in der Entity: polymorph statt instanceof. */
  public abstract BigDecimal minBalance();

  public Long getId() { return id; }
  public PersonEntity getOwner() { return owner; }
  public void setOwner(PersonEntity owner) { this.owner = owner; }
  public BigDecimal getBalance() { return balance; }
  public void setBalance(BigDecimal balance) { this.balance = balance; }
  public int getVersion() { return version; }
}
