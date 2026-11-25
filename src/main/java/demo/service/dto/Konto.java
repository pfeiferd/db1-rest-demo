package demo.service.dto;

import java.math.BigDecimal;

/** Abstrakte Oberklasse der Konten – mit Versionsnummer für optimistisches Sperren. */
public abstract class Konto extends DTO {
  private long personId;              // Inhaber: ID statt Referenz
  private BigDecimal saldo;
  private int version;                // Stand beim Lesen, geht mit zurück

  public long getPersonId() { return personId; }
  public void setPersonId(long personId) { this.personId = personId; }
  public BigDecimal getSaldo() { return saldo; }
  public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
  public int getVersion() { return version; }
  public void setVersion(int version) { this.version = version; }
}
