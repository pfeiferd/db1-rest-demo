package demo;

import java.util.Set;
import javax.sql.DataSource;

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
    DataSource dataSource = DemoDatabase.create();
    // Datenzugriff mit JDBC oder - mit -Dpersistence=jpa - mit JPA
    PersonService datenzugriff = "jpa".equals(System.getProperty("persistence"))
        ? new PersonJPAService(DemoDatabase.createEntityManagerFactory(dataSource))
        : new PersonJDBCService(dataSource);
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
