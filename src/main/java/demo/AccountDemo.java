package demo;

import java.math.BigDecimal;
import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

import demo.service.AccountService;
import demo.service.ServiceException;
import demo.service.dto.Account;
import demo.service.jdbc.AccountJDBCService;
import demo.service.jpa.AccountJPAService;

/**
 * Konten mit Vererbung, Versionsnummer und Transaktion – einmal mit JDBC,
 * einmal mit JPA, jeweils auf frisch angelegten Daten.
 * Start: mvn exec:java -Dexec.mainClass=demo.AccountDemo
 */
public class AccountDemo {
  public static void main(String[] args) {
    DataSource dataSource = DemoDatabase.create();
    demonstrate("JDBC", new AccountJDBCService(dataSource));

    DemoDatabase.create();                // gleiche Ausgangsdaten für JPA
    try (EntityManagerFactory emf = DemoDatabase.createEntityManagerFactory(dataSource)) {
      demonstrate("JPA", new AccountJPAService(emf));
    }
  }

  private static void demonstrate(String name, AccountService accounts) {
    System.out.println();
    System.out.println("=========== " + name + " ===========");

    step("1. Alle Konten – polymorph aus zwei Tabellen");
    for (Account a : accounts.getAll()) {
      System.out.println(a);
    }

    step("2. 300 von Girokonto 1 auf Sparkonto 2 – eine Transaktion, zwei UPDATEs");
    accounts.transfer(1, 2, new BigDecimal("300.00"));
    System.out.println(accounts.get(1) + " / " + accounts.get(2));

    step("3. 5000 von Sparkonto 2 – Deckung reicht nicht, nichts wird geändert");
    attempt(() -> accounts.transfer(2, 1, new BigDecimal("5000.00")));
    System.out.println(accounts.get(1) + " / " + accounts.get(2));

    step("4. -3000 von Girokonto 1 auf Sparkonto 2 – das 2. UPDATE verletzt"
        + " CHECK (balance >= 0), der Rollback nimmt auch das 1. zurück");
    attempt(() -> accounts.transfer(1, 2, new BigDecimal("-3000.00")));
    System.out.println(accounts.get(1) + " / " + accounts.get(2));

    step("5. Veraltetes DTO zurückschreiben – Versionskonflikt");
    Account stale = accounts.get(3);      // Version merken ...
    accounts.transfer(3, 1, new BigDecimal("50.00"));    // ... inzwischen ändert sich Konto 3
    stale.setBalance(new BigDecimal("1000000.00"));
    attempt(() -> accounts.update(stale));
    System.out.println(accounts.get(3));
  }

  private static void attempt(Runnable action) {
    try {
      action.run();
    } catch (ServiceException e) {
      Throwable cause = e;                 // die innerste Ursache zeigen
      while (cause.getCause() != null) {
        cause = cause.getCause();
      }
      String text = cause.getMessage().lines().findFirst().orElse("");
      System.out.println(e.getClass().getSimpleName() + " – Ursache "
          + cause.getClass().getSimpleName() + ": "
          + text.substring(0, Math.min(90, text.length())));
    }
  }

  private static void step(String title) {
    System.out.println();
    System.out.println("--- " + title + " ---");
  }
}
