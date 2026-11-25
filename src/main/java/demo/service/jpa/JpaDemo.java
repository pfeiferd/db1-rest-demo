package demo.service.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.RollbackException;

import demo.DemoDatabase;

/**
 * Zeigt die prinzipiellen Probleme der O/R-Abbildung im SQL-Protokoll.
 * Start: mvn exec:java -Dexec.mainClass=demo.service.jpa.JpaDemo
 */
public class JpaDemo {
  public static void main(String[] args) {
    try (EntityManagerFactory emf =
             DemoDatabase.createEntityManagerFactory(DemoDatabase.create())) {

      schritt("1. N+1: erst die Logins, dann je Login die Person");
      emf.runInTransaction(em -> {
        for (UserEntity u : em.createQuery("SELECT u FROM User u", UserEntity.class)
            .getResultList()) {
          System.out.println(u.getLogin() + " -> " + u.getPerson().getLastName());
        }
      });

      schritt("2. JOIN FETCH: alles in einer Anfrage");
      emf.runInTransaction(em -> {
        for (UserEntity u : em.createQuery(
            "SELECT u FROM User u JOIN FETCH u.person", UserEntity.class)
            .getResultList()) {
          System.out.println(u.getLogin() + " -> " + u.getPerson().getLastName());
        }
      });

      schritt("3. Objektidentität im Persistence Context");
      emf.runInTransaction(em -> {
        PersonEntity a = em.find(PersonEntity.class, 1L);
        PersonEntity b = em.createQuery(
            "SELECT p FROM Person p WHERE p.lastName = 'Lovelace'", PersonEntity.class)
            .getSingleResult();
        System.out.println("dasselbe Objekt: " + (a == b));
      });

      schritt("4. Lazy Loading ohne Persistence Context");
      UserEntity ada = emf.callInTransaction(em -> em.find(UserEntity.class, 1L));
      try {
        System.out.println(ada.getPerson().getLastName());
      } catch (RuntimeException e) {
        System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
      }

      schritt("5. Zur Laufzeit erzeugte Klassen: Proxy und Collection");
      emf.runInTransaction(em -> {
        UserEntity u = em.find(UserEntity.class, 1L);
        PersonEntity p = u.getPerson();                 // noch nicht geladen
        System.out.println("u.getPerson():  " + p.getClass().getName());
        System.out.println("Nachname:       " + p.getLastName());   // jetzt erst SELECT
        System.out.println("p.getLogins():  " + p.getLogins().getClass().getName());
      });

      schritt("6. Optimistisches Sperren mit @Version");
      try (EntityManager em1 = emf.createEntityManager();
           EntityManager em2 = emf.createEntityManager()) {
        em1.getTransaction().begin();
        em2.getTransaction().begin();
        PersonEntity p1 = em1.find(PersonEntity.class, 1L);   // beide lesen Version 0
        PersonEntity p2 = em2.find(PersonEntity.class, 1L);
        p1.setLastName("King");
        em1.getTransaction().commit();                         // Version 0 -> 1
        p2.setFirstName("Augusta Ada");
        try {
          em2.getTransaction().commit();                       // erwartet noch Version 0
        } catch (RollbackException e) {
          System.out.println("Commit gescheitert: "
              + e.getCause().getClass().getSimpleName());
        }
      }
    }
  }

  private static void schritt(String titel) {
    System.out.println();
    System.out.println("--- " + titel + " ---");
  }
}
