package com.cloudlabs.server.security.auth;

import com.cloudlabs.server.security.auth.dto.AuthenticationResponseDTO;
import com.cloudlabs.server.security.auth.dto.LoginDTO;
import com.cloudlabs.server.security.auth.dto.RegisterDTO;

public interface AuthenticationService {
  AuthenticationResponseDTO login(LoginDTO requestDTO);

  AuthenticationResponseDTO register(RegisterDTO registerDTO);
}
