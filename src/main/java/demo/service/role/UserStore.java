package demo.service.role;

import demo.service.dto.User;

public interface UserStore {
  User getUser();
  void setUser(User user);
}
