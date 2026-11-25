package demo.service.dto;

import java.math.BigDecimal;

public class Sparkonto extends Konto {
  private BigDecimal zinssatz;        // in Prozent

  public BigDecimal getZinssatz() { return zinssatz; }
  public void setZinssatz(BigDecimal zinssatz) { this.zinssatz = zinssatz; }

  @Override
  public String toString() {
    return "Sparkonto " + getId() + ": " + getSaldo() + " (Version " + getVersion() + ")";
  }
}
