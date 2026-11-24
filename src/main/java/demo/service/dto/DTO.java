package demo.service.dto;

/** Gemeinsame Oberklasse aller DTOs: liefert die id. */
public abstract class DTO {
  public static final long INVALID_ID = -1;   // noch keine id vergeben

  private long id = INVALID_ID;

  public long getId() { return id; }
  public void setId(long id) { this.id = id; }
}
