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

/** Abstrakte Oberklasse: keine eigene Tabelle, eine Tabelle pro konkreter Klasse. */
@Entity(name = "Konto")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class KontoEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "konto_seq")
  @SequenceGenerator(name = "konto_seq", sequenceName = "konto_seq", allocationSize = 1)
  private Long id;                    // eindeutig über beide Tabellen

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_id")
  private PersonEntity inhaber;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal saldo;

  @Version
  private int version;

  protected KontoEntity() {           // für JPA
  }

  protected KontoEntity(PersonEntity inhaber, BigDecimal saldo) {
    this.inhaber = inhaber;
    this.saldo = saldo;
  }

  /** Fachlogik in der Entity: polymorph statt instanceof. */
  public abstract BigDecimal untergrenze();

  public Long getId() { return id; }
  public PersonEntity getInhaber() { return inhaber; }
  public void setInhaber(PersonEntity inhaber) { this.inhaber = inhaber; }
  public BigDecimal getSaldo() { return saldo; }
  public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
  public int getVersion() { return version; }
}
