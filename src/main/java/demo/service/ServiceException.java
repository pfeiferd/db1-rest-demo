package demo.service;

/** Fehler in einem Dienst – unabhängig von der Technik darunter (JDBC, HTTP, …). */
public class ServiceException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ServiceException(String message) { super(message); }
  public ServiceException(Throwable cause) { super(cause); }
}
