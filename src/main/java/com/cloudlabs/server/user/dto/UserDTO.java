package com.cloudlabs.server.user.dto;

import com.cloudlabs.server.role.dto.RoleDTO;
import java.util.List;

public class UserDTO {

  private String fullname;

  private String username;

  private String email;

  private String password;

  private List<RoleDTO> roles;

  public UserDTO() {
  }

  public UserDTO(String fullname, String username, String email,
      String password) {
    this.fullname = fullname;
    this.username = username;
    this.email = email;
    this.password = password;
  }

  public UserDTO(String fullname, String username, String email,
      String password, List<RoleDTO> roles) {
    this.fullname = fullname;
    this.username = username;
    this.email = email;
    this.password = password;
    this.roles = roles;
  }

  public String getFullname() {
    return fullname;
  }

  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<RoleDTO> getRoles() {
    return roles;
  }

  public void setRoles(List<RoleDTO> roles) {
    this.roles = roles;
  }
}
