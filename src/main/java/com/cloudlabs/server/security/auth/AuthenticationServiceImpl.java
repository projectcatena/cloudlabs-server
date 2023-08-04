package com.cloudlabs.server.security.auth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleRepository;
import com.cloudlabs.server.role.RoleType;
import com.cloudlabs.server.security.auth.dto.AuthenticationResponseDTO;
import com.cloudlabs.server.security.auth.dto.LoginDTO;
import com.cloudlabs.server.security.auth.dto.RegisterDTO;
import com.cloudlabs.server.security.jwt.JwtService;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

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

    // Map additional claims, specifically username, fullname, and list of roles
    // for frontend
    HashMap<String, Object> extraClaims = new HashMap<String, Object>();
    extraClaims.put("id", user.getId());
    extraClaims.put("username", user.getUserName());
    extraClaims.put("fullname", user.getFullname());
    extraClaims.put("roles", user.getRoles());

    String jwt = jwtService.generateToken(extraClaims, user);

    return new AuthenticationResponseDTO(jwt);
  }

  /*
   * Assign normal role to new user, more roles may be added manually
   */
  @Override
  public AuthenticationResponseDTO register(RegisterDTO registerDTO) {
    Role userRole = roleRepository.findByName(RoleType.USER);

    if (userRole == null) {
      userRole = new Role(RoleType.USER);
    }

    if (userRepository.findByEmail(registerDTO.getEmail()).isPresent() ||
        userRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "User already exists!");
    }

    Set<Role> roles = new HashSet<>(Arrays.asList(userRole));
    User user = new User(registerDTO.getFullname(), registerDTO.getUsername(),
        registerDTO.getEmail(),
        passwordEncoder.encode(registerDTO.getPassword()), roles);

    // Id will be auto-generated and assigned
    userRepository.save(user);

    HashMap<String, Object> extraClaims = new HashMap<String, Object>();
    extraClaims.put("id", user.getId());
    extraClaims.put("username", user.getUserName());
    extraClaims.put("fullname", user.getFullname());
    extraClaims.put("roles", user.getRoles());

    String jwt = jwtService.generateToken(extraClaims, user);

    return new AuthenticationResponseDTO(jwt);
  }
}
