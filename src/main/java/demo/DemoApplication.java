package demo;

import java.util.Set;
import javax.sql.DataSource;

import jakarta.ws.rs.core.Application;

import demo.personen.PersonResource;
import demo.service.PersonService;
import demo.service.cache.PersonCacheService;
import demo.service.jdbc.PersonJDBCService;
import demo.service.rest.PersonRestService;
import demo.service.rest.ServiceExceptionMapper;
import demo.service.role.DemoUserStore;
import demo.service.role.PersonRoleService;

/**
 * Die Jakarta-REST-Anwendung. Sie wird in WEB-INF/web.xml unter /api/*
 * eingebunden.
 */
public class DemoApplication extends Application {
  private final Set<Object> singletons;

  public DemoApplication() {
    DataSource dataSource = DemoDatabase.create();
    // Die Delegationskette der Mini-Studie, mit Write-Through-Cache
    PersonService personService =
        new PersonRestService(
            new PersonRoleService(
                new PersonCacheService(
                    new PersonJDBCService(dataSource)),
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
