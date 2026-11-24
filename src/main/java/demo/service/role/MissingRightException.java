package demo.service.role;

import demo.service.ServiceException;

/** Der angemeldeten Person fehlt das Recht für eine Operation. */
public class MissingRightException extends ServiceException {
  private static final long serialVersionUID = 1L;

  public MissingRightException(String message) { super(message); }
}
