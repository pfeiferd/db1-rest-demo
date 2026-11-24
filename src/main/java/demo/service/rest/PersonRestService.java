package demo.service.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import demo.service.PersonService;
import demo.service.dto.Person;

@Path("/PersonService")
public class PersonRestService implements PersonService {
  private final PersonService delegate;

  public PersonRestService(PersonService delegate) {
    this.delegate = delegate;
  }

  @Override
  @GET
  @Path("get/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Person get(@PathParam("id") long id) {
    return delegate.get(id);
  }

  @Override
  @GET
  @Path("getAll")
  @Produces(MediaType.APPLICATION_JSON)
  public Person[] getAll() {
    return delegate.getAll();
  }

  @Override
  @POST
  @Path("create")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public long create(Person p) {
    return delegate.create(p);
  }

  @Override
  @PUT
  @Path("update")
  @Consumes(MediaType.APPLICATION_JSON)
  public void update(Person p) {
    delegate.update(p);
  }

  @Override
  @DELETE
  @Path("remove/{id}")
  public void remove(@PathParam("id") long id) {
    delegate.remove(id);
  }
}
