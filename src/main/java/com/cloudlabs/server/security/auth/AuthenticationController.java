package com.cloudlabs.server.security.auth;

import com.cloudlabs.server.security.auth.dto.AuthenticationResponseDTO;
import com.cloudlabs.server.security.auth.dto.LoginDTO;
import com.cloudlabs.server.security.auth.dto.RegisterDTO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

  @Autowired
  private AuthenticationService authenticationService;

  // After register, may allow user to login, or alternatively, redirect them to
  // login page
  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponseDTO> register(
      @RequestBody RegisterDTO request, HttpServletResponse response) {
    AuthenticationResponseDTO authenticationResponseDTO = authenticationService.register(request);

    final Cookie cookie = new Cookie("jwt", authenticationResponseDTO.getJwt());
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setDomain("localhost");
    cookie.setMaxAge(5 * 60); // 5 minutes

    response.addCookie(cookie);
    return ResponseEntity.ok(authenticationResponseDTO);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody LoginDTO request, HttpServletResponse response) {
    AuthenticationResponseDTO authenticationResponseDTO = authenticationService.login(request);

    final Cookie cookie = new Cookie("jwt", authenticationResponseDTO.getJwt());
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setDomain("localhost");
    cookie.setMaxAge(5 * 60); // 5 minutes

    response.addCookie(cookie);
    return ResponseEntity.ok(authenticationResponseDTO);
  }

  @PostMapping("/signout")
  public ResponseEntity<String> login(HttpServletResponse response) {

    final Cookie cookie = new Cookie("jwt", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setDomain("localhost");
    cookie.setMaxAge(0); // logout set to 0 to clear cookie

    response.addCookie(cookie);
    return ResponseEntity.ok("""
          "status": "OK"
        """);
  }

  // TODO: refresh token
}
