package demo.service;

import demo.service.dto.User;

public interface UserService extends CRUDService<User> {
  User login(String login, String password);
  void logout();
}
