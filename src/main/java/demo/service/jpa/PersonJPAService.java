package demo.service.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import demo.service.NotFoundException;
import demo.service.PersonService;
import demo.service.dto.Person;

/** Dieselbe Schnittstelle wie PersonJDBCService – mit JPA statt JDBC. */
public class PersonJPAService implements PersonService,
    ConversionService<Person, PersonEntity> {
  private final EntityManagerFactory emf;

  public PersonJPAService(EntityManagerFactory emf) {
    this.emf = emf;
  }

  @Override
  public Person get(long id) {
    return emf.callInTransaction(em -> toDTO(find(em, id)));
  }

  @Override
  public long create(Person p) {
    PersonEntity e = fromDTO(p);
    emf.runInTransaction(em -> em.persist(e));        // INSERT
    p.setId(e.getId());
    return p.getId();
  }

  @Override
  public Person[] getAll() {
    return emf.callInTransaction(em -> em
        .createQuery("SELECT p FROM Person p ORDER BY p.id", PersonEntity.class)
        .getResultStream()
        .map(this::toDTO)
        .toArray(Person[]::new));
  }

  @Override
  public void update(Person p) {
    emf.runInTransaction(em -> {
      PersonEntity e = find(em, p.getId());
      e.setFirstName(p.getFirstName());   // DTO → Entity; kein UPDATE-Aufruf:
      e.setLastName(p.getLastName());     // JPA erkennt die Änderung beim Commit
    });
  }

  @Override
  public void remove(long id) {
    emf.runInTransaction(em -> em.remove(find(em, id)));
  }

  private static PersonEntity find(EntityManager em, long id) {
    PersonEntity e = em.find(PersonEntity.class, id);
    if (e == null) {
      throw new NotFoundException("Person " + id + " nicht gefunden");
    }
    return e;
  }

  @Override
  public PersonEntity fromDTO(Person p) {              // DTO → Entity
    return new PersonEntity(p.getFirstName(), p.getLastName());
  }

  @Override
  public Person toDTO(PersonEntity e) {                // Entity → DTO
    return new Person(e.getId(), e.getFirstName(), e.getLastName());
  }
}
