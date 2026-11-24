package demo.personen;

import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import demo.service.dto.Person;

/** Ruft die Ressource /api/personen mit dem Client-API von Jakarta REST auf. */
public class PersonClient {
  public static void main(String[] args) {
    Client client = ClientBuilder.newClient();
    WebTarget personen =
        client.target("http://localhost:8080/api/personen");

    // GET: eine Person als Java-Objekt
    Person ada = personen.path("1")
        .request(MediaType.APPLICATION_JSON)
        .get(Person.class);
    System.out.println(ada.getLastName());

    // POST: neue Person anlegen
    Response r = personen.request()
        .post(Entity.json(new Person(0, "Grace", "Hopper")));
    System.out.println(r.getStatus() + " " + r.getLocation());

    // GET: alle Personen als Liste
    List<Person> alle = personen
        .request(MediaType.APPLICATION_JSON)
        .get(new GenericType<List<Person>>() {});
    System.out.println(alle.size() + " Personen");
    client.close();
  }
}
