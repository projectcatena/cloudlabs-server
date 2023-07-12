package com.cloudlabs.server.security.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

  @PostMapping("/register")
  public ResponseEntity<String> register() {
  }

  // TODO: set cookie
  @PostMapping("/login")
  public ResponseEntity<String> login() {
  }

  // TODO: refresh token
  // TODO: signout
}
