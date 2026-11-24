package demo.service.dto;

public class User extends DTO {
  private String login;
  private String role;
  private long personId;

  public String getLogin() { return login; }
  public void setLogin(String login) { this.login = login; }
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
  public long getPersonId() { return personId; }
  public void setPersonId(long personId) { this.personId = personId; }
}
