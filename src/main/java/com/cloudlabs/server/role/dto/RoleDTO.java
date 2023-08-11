package com.cloudlabs.server.role.dto;

import com.cloudlabs.server.role.RoleType;

public class RoleDTO {
  private RoleType name;

  public RoleDTO() {
  }

  public RoleDTO(RoleType name) {
    this.name = name;
  }

  public RoleType getName() {
    return name;
  }

  public void setName(RoleType name) {
    this.name = name;
  }
}
