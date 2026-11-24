package demo.service.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import demo.service.NotFoundException;
import demo.service.ServiceException;
import demo.service.role.MissingRightException;

/** Übersetzt Exceptions der Dienste in passende HTTP-Statuscodes. */
@Provider
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {
  @Override
  public Response toResponse(ServiceException e) {
    Response.Status status;
    if (e instanceof NotFoundException) {
      status = Response.Status.NOT_FOUND;            // 404
    } else if (e instanceof MissingRightException) {
      status = Response.Status.FORBIDDEN;            // 403
    } else {
      status = Response.Status.INTERNAL_SERVER_ERROR; // 500
    }
    return Response.status(status)
        .type(MediaType.TEXT_PLAIN_TYPE.withCharset("UTF-8"))
        .entity(e.getMessage())
        .build();
  }
}
