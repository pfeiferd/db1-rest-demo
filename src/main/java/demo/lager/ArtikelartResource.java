package demo.lager;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** REST-Ressource für Artikelarten der Lagerverwaltung unter /api/artikelarten. */
@Path("artikelarten")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtikelartResource {
  // Zum Ausprobieren im Hauptspeicher – später per JDBC in der Datenbank.
  // Thread-sicher und sortiert, weil Requests gleichzeitig ankommen.
  private static final Map<String, Artikelart> DATEN = new ConcurrentSkipListMap<>(Map.of(
      "A1", new Artikelart("A1", "Haferflocken", 0.50),
      "A2", new Artikelart("A2", "Mineralwasser", 1.50)));

  @GET
  public Collection<Artikelart> getAll(@QueryParam("maxGewicht") Double maxGewicht) {
    if (maxGewicht == null) {
      return DATEN.values();
    }
    return DATEN.values().stream()
        .filter(a -> a.getGewicht() <= maxGewicht)
        .toList();
  }

  @GET
  @Path("{aid}")
  public Artikelart get(@PathParam("aid") String aid) {
    Artikelart a = DATEN.get(aid);
    if (a == null) {
      throw new NotFoundException("Artikelart " + aid + " gibt es nicht");
    }
    return a;
  }

  @POST
  public Response create(Artikelart a) {
    if (a.getAid() == null || a.getGewicht() <= 0) {
      throw new BadRequestException("aid fehlt oder Gewicht <= 0");
    }
    DATEN.put(a.getAid(), a);
    return Response.created(URI.create("artikelarten/" + a.getAid()))
        .entity(a).build();
  }

  @PUT
  @Path("{aid}")
  public Artikelart update(@PathParam("aid") String aid, Artikelart a) {
    get(aid);              // 404, falls es sie nicht gibt
    a.setAid(aid);
    DATEN.put(aid, a);
    return a;
  }

  @DELETE
  @Path("{aid}")
  public void remove(@PathParam("aid") String aid) {
    if (DATEN.remove(aid) == null) {
      throw new NotFoundException("Artikelart " + aid + " gibt es nicht");
    }
  }
}
