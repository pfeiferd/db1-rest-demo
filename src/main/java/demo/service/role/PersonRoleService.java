package demo.service.role;

import demo.service.PersonService;
import demo.service.dto.Person;
import demo.service.dto.User;

public class PersonRoleService implements PersonService {
  private final PersonService delegate;
  private final UserStore userStore;

  public PersonRoleService(PersonService delegate, UserStore userStore) {
    this.delegate = delegate;
    this.userStore = userStore;
  }

  private void checkAdmin() {
    User user = userStore.getUser();
    if (user == null || !"ADMIN".equals(user.getRole())) {
      throw new MissingRightException("Nur für Admins");
    }
  }

  @Override
  public Person get(long id) {
    User user = userStore.getUser();
    // Die eigene Person darf jede(r) Angemeldete lesen
    if (user == null || user.getPersonId() != id) {
      checkAdmin();
    }
    return delegate.get(id);
  }

  @Override
  public long create(Person p) {
    checkAdmin();
    return delegate.create(p);
  }

  @Override
  public Person[] getAll() {
    checkAdmin();
    return delegate.getAll();
  }

  @Override
  public void update(Person p) {
    checkAdmin();
    delegate.update(p);
  }

  @Override
  public void remove(long id) {
    checkAdmin();
    delegate.remove(id);
  }
}
