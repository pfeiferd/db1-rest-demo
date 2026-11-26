package demo;

import java.util.Set;
import jakarta.persistence.EntityManagerFactory;

import jakarta.ws.rs.core.Application;

import demo.personen.PersonResource;
import demo.service.PersonService;
import demo.service.cache.PersonCacheService;
import demo.service.jdbc.PersonJDBCService;
import demo.service.jpa.PersonJPAService;
import demo.service.rest.PersonRestService;
import demo.service.rest.ServiceExceptionMapper;
import demo.service.role.DemoUserStore;
import demo.service.role.PersonRoleService;

/**
 * Die JAX-RS-Anwendung. Sie wird in WEB-INF/web.xml unter /api/*
 * eingebunden.
 */
public class DemoApplication extends Application {
  private final Set<Object> singletons;

  public DemoApplication() {
    // Datenzugriff mit JDBC oder - mit -Dpersistence=jpa - mit JPA
    PersonService datenzugriff;
    if ("jpa".equals(System.getProperty("persistence"))) {
      // JPA legt das Schema selbst an, die Daten kommen per persist()
      EntityManagerFactory emf =
          DemoDatabase.createEntityManagerFactoryWithSchema(DemoDatabase.empty("demo"));
      DemoDatabase.insertData(emf);
      datenzugriff = new PersonJPAService(emf);
    } else {
      // JDBC: Schema und Daten per SQL-Skript
      datenzugriff = new PersonJDBCService(DemoDatabase.create());
    }
    // Die Delegationskette der Mini-Studie, mit Write-Through-Cache
    PersonService personService =
        new PersonRestService(
            new PersonRoleService(
                new PersonCacheService(datenzugriff),
                new DemoUserStore()));
    singletons = Set.of(personService);
  }

  /** Klassen, von denen Jersey für jeden Request selbst ein Objekt anlegt. */
  @Override
  public Set<Class<?>> getClasses() {
    return Set.of(PersonResource.class, ServiceExceptionMapper.class);
  }

  /** Fertige Objekte, die Jersey für alle Requests verwendet. */
  @Override
  @SuppressWarnings("deprecation")
  public Set<Object> getSingletons() {
    return singletons;
  }
}
