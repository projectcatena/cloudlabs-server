package com.cloudlabs.server.security.auth;

import com.cloudlabs.server.security.auth.dto.AuthenticationResponseDTO;

public interface AuthenticationService {
  AuthenticationResponseDTO login();

  AuthenticationResponseDTO register();
}
