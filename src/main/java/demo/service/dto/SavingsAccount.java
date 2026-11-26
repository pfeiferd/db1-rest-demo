package demo.service.dto;

import java.math.BigDecimal;

/** Sparkonto. */
public class SavingsAccount extends Account {
  private BigDecimal interestRate;    // in Prozent

  public BigDecimal getInterestRate() { return interestRate; }
  public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

  @Override
  public String toString() {
    return "Sparkonto " + getId() + ": " + getBalance() + " (Version " + getVersion() + ")";
  }
}
