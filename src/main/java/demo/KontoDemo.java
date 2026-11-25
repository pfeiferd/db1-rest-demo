package demo;

import java.math.BigDecimal;
import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

import demo.service.KontoService;
import demo.service.ServiceException;
import demo.service.dto.Konto;
import demo.service.jdbc.KontoJDBCService;
import demo.service.jpa.KontoJPAService;

/**
 * Konten mit Vererbung, Versionsnummer und Transaktion – einmal mit JDBC,
 * einmal mit JPA, jeweils auf frisch angelegten Daten.
 * Start: mvn exec:java -Dexec.mainClass=demo.KontoDemo
 */
public class KontoDemo {
  public static void main(String[] args) {
    DataSource dataSource = DemoDatabase.create();
    vorfuehren("JDBC", new KontoJDBCService(dataSource));

    DemoDatabase.create();                // gleiche Ausgangsdaten für JPA
    try (EntityManagerFactory emf = DemoDatabase.createEntityManagerFactory(dataSource)) {
      vorfuehren("JPA", new KontoJPAService(emf));
    }
  }

  private static void vorfuehren(String name, KontoService konten) {
    System.out.println();
    System.out.println("=========== " + name + " ===========");

    schritt("1. Alle Konten – polymorph aus zwei Tabellen");
    for (Konto k : konten.getAll()) {
      System.out.println(k);
    }

    schritt("2. 300 von Girokonto 1 auf Sparkonto 2 – eine Transaktion, zwei UPDATEs");
    konten.ueberweisen(1, 2, new BigDecimal("300.00"));
    System.out.println(konten.get(1) + " / " + konten.get(2));

    schritt("3. 5000 von Sparkonto 2 – Deckung reicht nicht, nichts wird geändert");
    versuche(() -> konten.ueberweisen(2, 1, new BigDecimal("5000.00")));
    System.out.println(konten.get(1) + " / " + konten.get(2));

    schritt("4. -3000 von Girokonto 1 auf Sparkonto 2 – das 2. UPDATE verletzt"
        + " CHECK (saldo >= 0), der Rollback nimmt auch das 1. zurück");
    versuche(() -> konten.ueberweisen(1, 2, new BigDecimal("-3000.00")));
    System.out.println(konten.get(1) + " / " + konten.get(2));

    schritt("5. Veraltetes DTO zurückschreiben – Versionskonflikt");
    Konto alt = konten.get(3);            // Version merken ...
    konten.ueberweisen(3, 1, new BigDecimal("50.00"));   // ... inzwischen ändert sich Konto 3
    alt.setSaldo(new BigDecimal("1000000.00"));
    versuche(() -> konten.update(alt));
    System.out.println(konten.get(3));
  }

  private static void versuche(Runnable aktion) {
    try {
      aktion.run();
    } catch (ServiceException e) {
      Throwable ursache = e;               // die innerste Ursache zeigen
      while (ursache.getCause() != null) {
        ursache = ursache.getCause();
      }
      String text = ursache.getMessage().lines().findFirst().orElse("");
      System.out.println(e.getClass().getSimpleName() + " – Ursache "
          + ursache.getClass().getSimpleName() + ": "
          + text.substring(0, Math.min(90, text.length())));
    }
  }

  private static void schritt(String titel) {
    System.out.println();
    System.out.println("--- " + titel + " ---");
  }
}
