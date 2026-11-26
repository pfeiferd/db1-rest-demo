package demo.service.dto;

import java.math.BigDecimal;

/** Abstrakte Oberklasse der Konten – mit Versionsnummer für optimistisches Sperren. */
public abstract class Account extends DTO {
  private long personId;             // Inhaber(in): ID statt Referenz
  private BigDecimal balance;
  private int version;               // Stand beim Lesen, geht zurück

  public long getPersonId() { return personId; }
  public void setPersonId(long personId) { this.personId = personId; }
  public BigDecimal getBalance() { return balance; }
  public void setBalance(BigDecimal balance) { this.balance = balance; }
  public int getVersion() { return version; }
  public void setVersion(int version) { this.version = version; }
}
