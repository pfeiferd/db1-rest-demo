package demo.service.jpa;

import java.math.BigDecimal;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Sparkonto. */
@Entity(name = "SavingsAccount")
@Table(name = "savings_account",
    check = @CheckConstraint(name = "balance_not_negative",
                             constraint = "balance >= 0"))
public class SavingsAccountEntity extends AccountEntity {
  @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal interestRate;

  protected SavingsAccountEntity() {  // für JPA
  }

  public SavingsAccountEntity(PersonEntity owner, BigDecimal balance, BigDecimal interestRate) {
    super(owner, balance);
    this.interestRate = interestRate;
  }

  @Override
  public BigDecimal minBalance() {
    return BigDecimal.ZERO;
  }

  public BigDecimal getInterestRate() { return interestRate; }
  public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
}
