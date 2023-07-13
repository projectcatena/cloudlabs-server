package com.cloudlabs.server.security.auth.dto;

public class AuthenticationResponseDTO {

  private String jwt;

  public String getJwt() {
    return jwt;
  }

  public void setJwt(String jwt) {
    this.jwt = jwt;
  }

  public AuthenticationResponseDTO(AuthenticationResponseDtoBuilder builder) {
    this.jwt = builder.jwt;
  }

  public static class AuthenticationResponseDtoBuilder {
    private String jwt;

    public AuthenticationResponseDtoBuilder() {
    }

    public AuthenticationResponseDtoBuilder setJwt(String jwt) {
      this.jwt = jwt;
      return this;
    }

    public AuthenticationResponseDTO build() {
      return new AuthenticationResponseDTO(this);
    }
  }
}
