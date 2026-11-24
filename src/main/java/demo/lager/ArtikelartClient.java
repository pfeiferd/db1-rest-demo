package demo.lager;

import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ArtikelartClient {
  public static void main(String[] args) {
    Client client = ClientBuilder.newClient();
    WebTarget artikelarten =
        client.target("http://localhost:8080/api/artikelarten");

    // GET: eine Artikelart als Java-Objekt
    Artikelart a1 = artikelarten.path("A1")
        .request(MediaType.APPLICATION_JSON)
        .get(Artikelart.class);
    System.out.println(a1.getAname());

    // POST: neue Artikelart anlegen
    Response r = artikelarten.request()
        .post(Entity.json(new Artikelart("A3", "Zucker", 1.00)));
    System.out.println(r.getStatus() + " " + r.getLocation());

    // GET: alle Artikelarten als Liste
    List<Artikelart> alle = artikelarten
        .request(MediaType.APPLICATION_JSON)
        .get(new GenericType<List<Artikelart>>() {});
    System.out.println(alle.size() + " Artikelarten");
    client.close();
  }
}
