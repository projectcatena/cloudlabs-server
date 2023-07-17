package com.cloudlabs.server.security;

import com.cloudlabs.server.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {

  @Autowired
  private UserRepository userRepository;

  // Override the loadByUsername method to find user by email
  @Bean
  public UserDetailsService userDetailsService() {
    return email -> userRepository.findByEmail(email).orElseThrow(
        () -> new UsernameNotFoundException("User not found"));
  }

  /*
   * This is the data access object that is responsible for fetching user
   * details and encode password .etc
   *
   * When authentication is successful, the Authentication that is returned is
   * of type UsernamePasswordAuthenticationToken and has a principal that is the
   * UserDetails returned by the configured UserDetailsService. Ultimately, the
   * returned UsernamePasswordAuthenticationToken is set on the
   * SecurityContextHolder by the authentication Filter.
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    // An implementation of an Authentication Provider that uses a
    // UserDetailsService and PasswordEncoder to authenticate a username and
    // password.
    // Source:
    // https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html
    DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

    // DaoAuthenticationProvider looks up the UserDetails from the
    // UserDetailsService.
    daoAuthenticationProvider.setUserDetailsService(userDetailsService());

    // DaoAuthenticationProvider then uses the PasswordEncoder to validate the
    // password on the UserDetails returned in the previous step.
    daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

    return daoAuthenticationProvider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Literally manages the authentication
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
      throws Exception {

    return authenticationConfiguration.getAuthenticationManager();
  }
}
