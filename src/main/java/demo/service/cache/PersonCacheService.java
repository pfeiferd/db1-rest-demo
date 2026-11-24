package demo.service.cache;

import java.util.HashMap;
import java.util.Map;

import demo.service.PersonService;
import demo.service.dto.Person;

public class PersonCacheService implements PersonService {
  private final PersonService delegate;
  private final Map<Long, Person> cache = new HashMap<>();

  public PersonCacheService(PersonService delegate) {
    this.delegate = delegate;
  }

  @Override
  public synchronized Person get(long id) {
    Person p = cache.get(id);
    if (p == null) {                 // Fehltreffer: aus der Datenbank holen
      p = delegate.get(id);
      cache.put(id, p);
    }
    return copy(p);                  // nie das Objekt im Cache herausgeben
  }

  @Override
  public synchronized long create(Person p) {
    long id = delegate.create(p);    // zuerst in die Datenbank …
    cache.put(id, copy(p));          // … dann in den Cache
    return id;
  }

  @Override
  public synchronized void update(Person p) {
    delegate.update(p);
    cache.put(p.getId(), copy(p));
  }

  @Override
  public synchronized void remove(long id) {
    delegate.remove(id);
    cache.remove(id);
  }

  @Override
  public Person[] getAll() {
    return delegate.getAll();        // wird nicht gecacht
  }

  private static Person copy(Person p) {
    Person c = new Person();
    c.setId(p.getId());
    c.setFirstName(p.getFirstName());
    c.setLastName(p.getLastName());
    return c;
  }
}
