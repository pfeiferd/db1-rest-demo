package demo.service;

/** Der Datensatz wurde inzwischen von jemand anderem geändert (Versionskonflikt). */
public class ConflictException extends ServiceException {
  private static final long serialVersionUID = 1L;

  public ConflictException(String message) {
    super(message);
  }
}
