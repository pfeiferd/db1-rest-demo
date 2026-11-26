package demo.service.jpa;

import jakarta.persistence.EntityManagerFactory;

import demo.DemoDatabase;

/**
 * Rundgang durch JPA im SQL-Protokoll: Schema anlegen, speichern, lesen,
 * Lazy Loading, ändern, anfragen, löschen. Jede SQL-Anweisung erscheint auf
 * der Konsole – direkt bei dem Schritt, der sie auslöst.
 * Start: mvn exec:java -Dexec.mainClass=demo.service.jpa.JpaTour
 */
public class JpaTour {
  public static void main(String[] args) {
    step("0. Schema anlegen – JPA erzeugt die DDL aus den Annotationen");
    try (EntityManagerFactory emf =
             DemoDatabase.createEntityManagerFactoryWithSchema(DemoDatabase.empty("tour"))) {

      step("1. Speichern – persist() erzeugt die INSERTs");
      long[] ids = emf.callInTransaction(em -> {
        PersonEntity ada = new PersonEntity("Ada", "Lovelace");
        UserEntity login = new UserEntity("ada", "-", "ADMIN");
        ada.addLogin(login);                  // beide Seiten der Beziehung setzen
        em.persist(ada);
        em.persist(login);                    // keine Kaskadierung: einzeln speichern
        return new long[] {ada.getId(), login.getId()};
      });

      step("2. Lesen mit Lazy Loading – die Person erst beim ersten Zugriff");
      emf.runInTransaction(em -> {
        UserEntity u = em.find(UserEntity.class, ids[1]);   // SELECT app_user
        System.out.println("Login gelesen: " + u.getLogin());
        PersonEntity p = u.getPerson();                    // noch kein SELECT
        System.out.println("Person: " + p.getLastName());  // jetzt SELECT person
        System.out.println("Logins: " + p.getLogins().size()); // jetzt SELECT app_user
      });

      step("3. Ändern – nur ein Setter, das UPDATE kommt beim Commit");
      emf.runInTransaction(em -> {
        PersonEntity p = em.find(PersonEntity.class, ids[0]);
        p.setLastName("King");
        System.out.println("Setter aufgerufen – noch kein UPDATE, jetzt Commit:");
      });

      step("4. Anfrage mit JPQL – JOIN FETCH lädt die Person gleich mit");
      emf.runInTransaction(em -> em
          .createQuery("SELECT u FROM User u JOIN FETCH u.person", UserEntity.class)
          .getResultList()
          .forEach(u -> System.out.println(u.getLogin() + " -> "
              + u.getPerson().getFirstName() + " " + u.getPerson().getLastName())));

      step("5. Löschen – remove() erzeugt das DELETE beim Commit");
      emf.runInTransaction(em -> em.remove(em.find(UserEntity.class, ids[1])));
    }
  }

  private static void step(String title) {
    System.out.println();
    System.out.println("--- " + title + " ---");
  }
}
