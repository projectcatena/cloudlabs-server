package com.cloudlabs.server.security.auth;

import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleType;
import com.cloudlabs.server.security.auth.dto.AuthenticationResponseDTO;
import com.cloudlabs.server.security.auth.dto.LoginDTO;
import com.cloudlabs.server.security.auth.dto.RegisterDTO;
import com.cloudlabs.server.security.jwt.JwtService;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private AuthenticationManager authenticationManager;

  /*
   * IMPORTANT: Only allow user to login via email.
   * Refer to WebSecurityConfig's userDetailsService for more information
   */
  @Override
  public AuthenticationResponseDTO login(LoginDTO requestDTO) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
        requestDTO.getEmail(), requestDTO.getPassword()));

    User user = userRepository.findByEmail(requestDTO.getEmail())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    String jwt = jwtService.generateToken(new HashMap<>(), user);

    return new AuthenticationResponseDTO(jwt);
  }

  /*
   * Assign normal role to new user, more roles may be added manually
   */
  @Override
  public AuthenticationResponseDTO register(RegisterDTO registerDTO) {
    Role role = new Role(RoleType.USER);

    User user = new User(registerDTO.getFullname(), registerDTO.getUsername(),
        registerDTO.getEmail(),
        passwordEncoder.encode(registerDTO.getPassword()));

    // Id will be auto-generated and assigned
    userRepository.save(user);

    // Generte jwt token with user
    String jwt = jwtService.generateToken(new HashMap<>(), user);

    AuthenticationResponseDTO response = new AuthenticationResponseDTO(jwt);

    return response;
  }
}
