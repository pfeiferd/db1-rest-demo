package demo.service.jpa;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity(name = "Sparkonto")
@Table(name = "sparkonto")
public class SparkontoEntity extends KontoEntity {
  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal zinssatz;

  protected SparkontoEntity() {       // für JPA
  }

  public SparkontoEntity(PersonEntity inhaber, BigDecimal saldo, BigDecimal zinssatz) {
    super(inhaber, saldo);
    this.zinssatz = zinssatz;
  }

  @Override
  public BigDecimal untergrenze() {
    return BigDecimal.ZERO;
  }

  public BigDecimal getZinssatz() { return zinssatz; }
  public void setZinssatz(BigDecimal zinssatz) { this.zinssatz = zinssatz; }
}
