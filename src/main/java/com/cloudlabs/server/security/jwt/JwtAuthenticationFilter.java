package com.cloudlabs.server.security.jwt;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    String jwt = getTokenFromCookie(request);
    String username = jwtService.getUsername(jwt);

    // If got no token or username is null, pass it to the next filter.
    if (jwt == null || username == null) {
      filterChain.doFilter(request, response);
    }

    if (username != null &&
        SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (jwtService.validateToken(jwt, userDetails)) {
        // We don't have credentials
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        // Give additional details of our request
        authenticationToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(
            authenticationToken);
      }
    }

    // IMPORTANT: Always call doFilter to the next filters to execute
    filterChain.doFilter(request, response);
  }

  private String getTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      throw new AuthenticationServiceException("No cookie provided");
    }

    for (Cookie cookie : cookies) {
      if (cookie.getName() == "jwt") {
        return cookie.getValue();
      }
    }

    throw new AuthenticationServiceException("Authentication token not found");
  }
}
