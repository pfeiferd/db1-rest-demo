package demo.service.jpa;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Girokonto. */
@Entity(name = "CheckingAccount")
@Table(name = "checking_account")
public class CheckingAccountEntity extends AccountEntity {
  @Column(name = "overdraft_limit", nullable = false, precision = 12, scale = 2)
  private BigDecimal overdraftLimit;

  protected CheckingAccountEntity() { // für JPA
  }

  public CheckingAccountEntity(PersonEntity owner, BigDecimal balance, BigDecimal overdraftLimit) {
    super(owner, balance);
    this.overdraftLimit = overdraftLimit;
  }

  @Override
  public BigDecimal minBalance() {
    return overdraftLimit.negate();
  }

  public BigDecimal getOverdraftLimit() { return overdraftLimit; }
  public void setOverdraftLimit(BigDecimal overdraftLimit) { this.overdraftLimit = overdraftLimit; }
}
