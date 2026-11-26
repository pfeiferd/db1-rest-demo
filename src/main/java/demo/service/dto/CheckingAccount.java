package demo.service.dto;

import java.math.BigDecimal;

/** Girokonto. */
public class CheckingAccount extends Account {
  private BigDecimal overdraftLimit; // so weit darf es ins Minus

  public BigDecimal getOverdraftLimit() { return overdraftLimit; }
  public void setOverdraftLimit(BigDecimal overdraftLimit) { this.overdraftLimit = overdraftLimit; }

  @Override
  public String toString() {
    return "Girokonto " + getId() + ": " + getBalance() + " (Version " + getVersion() + ")";
  }
}
