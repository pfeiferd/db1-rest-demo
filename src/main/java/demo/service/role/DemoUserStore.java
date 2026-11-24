package demo.service.role;

import demo.service.dto.User;

/**
 * Nur für die Demo: Es ist immer dieselbe Admin-Person angemeldet.
 * In einer echten Anwendung käme der User aus der HTTP-Session nach
 * einem Login (siehe gweb: RestService).
 */
public class DemoUserStore implements UserStore {
  private User user;

  public DemoUserStore() {
    user = new User();
    user.setId(1);
    user.setLogin("admin");
    user.setRole("ADMIN");
    user.setPersonId(1);
  }

  @Override
  public User getUser() {
    return user;
  }

  @Override
  public void setUser(User user) {
    this.user = user;
  }
}
