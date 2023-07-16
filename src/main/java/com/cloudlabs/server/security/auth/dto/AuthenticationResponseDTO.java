package com.cloudlabs.server.security.auth.dto;

public class AuthenticationResponseDTO {

  private String jwt;

  public AuthenticationResponseDTO() {
  }

  public AuthenticationResponseDTO(String jwt) {
    this.jwt = jwt;
  }

  public String getJwt() {
    return jwt;
  }

  public void setJwt(String jwt) {
    this.jwt = jwt;
  }
}
