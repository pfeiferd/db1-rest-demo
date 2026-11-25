package demo.service.dto;

import java.math.BigDecimal;

public class Girokonto extends Konto {
  private BigDecimal dispo;           // so weit darf der Saldo ins Minus

  public BigDecimal getDispo() { return dispo; }
  public void setDispo(BigDecimal dispo) { this.dispo = dispo; }

  @Override
  public String toString() {
    return "Girokonto " + getId() + ": " + getSaldo() + " (Version " + getVersion() + ")";
  }
}
