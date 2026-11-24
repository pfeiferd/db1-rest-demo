package demo.personen;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import demo.service.dto.DTO;
import demo.service.dto.Person;

/** REST-Ressource für Personen unter /api/personen. */
@Path("personen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {
  // Zum Ausprobieren im Hauptspeicher – thread-sicher und sortiert
  private static final Map<Long, Person> DATEN = new ConcurrentSkipListMap<>(Map.of(
      1L, new Person(1, "Ada", "Lovelace"),
      2L, new Person(2, "Alan", "Turing")));
  private static final AtomicLong NAECHSTE_ID = new AtomicLong(3);

  @GET                                  // GET /api/personen?lastName=…
  public Collection<Person> getAll(@QueryParam("lastName") String lastName) {
    if (lastName == null) {
      return DATEN.values();
    }
    return DATEN.values().stream()
        .filter(p -> p.getLastName().equals(lastName))
        .toList();
  }

  @GET
  @Path("{id}")                         // GET /api/personen/1
  public Person get(@PathParam("id") long id) {
    Person p = DATEN.get(id);
    if (p == null) {
      throw new NotFoundException("Person " + id + " gibt es nicht");
    }
    return p;
  }

  @POST                                 // POST /api/personen
  public Response create(Person p) {
    if (p.getLastName() == null || p.getLastName().isBlank()) {
      throw new BadRequestException("Nachname fehlt");
    }
    p.setId(NAECHSTE_ID.getAndIncrement()); // die id vergibt der Server
    DATEN.put(p.getId(), p);
    return Response.created(URI.create("personen/" + p.getId()))
        .entity(p).build();
  }

  @PUT
  @Path("{id}")                         // PUT /api/personen/3
  public Person update(@PathParam("id") long id, Person p) {
    // Die URL nennt die Person; eine id im Rumpf darf fehlen, aber nicht abweichen
    if (p.getId() != DTO.INVALID_ID && p.getId() != id) {
      throw new BadRequestException("id im Rumpf passt nicht zur URL");
    }
    p.setId(id);
    if (DATEN.replace(id, p) == null) { // ersetzt nur, wenn es sie schon gibt
      throw new NotFoundException("Person " + id + " gibt es nicht");
    }
    return p;
  }

  @DELETE
  @Path("{id}")                         // DELETE /api/personen/3
  public void remove(@PathParam("id") long id) {
    if (DATEN.remove(id) == null) {
      throw new NotFoundException("Person " + id + " gibt es nicht");
    }
  }
}
