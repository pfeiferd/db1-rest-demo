package demo.service.jpa;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity(name = "Girokonto")
@Table(name = "girokonto")
public class GirokontoEntity extends KontoEntity {
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal dispo;

  protected GirokontoEntity() {       // für JPA
  }

  public GirokontoEntity(PersonEntity inhaber, BigDecimal saldo, BigDecimal dispo) {
    super(inhaber, saldo);
    this.dispo = dispo;
  }

  @Override
  public BigDecimal untergrenze() {
    return dispo.negate();
  }

  public BigDecimal getDispo() { return dispo; }
  public void setDispo(BigDecimal dispo) { this.dispo = dispo; }
}
