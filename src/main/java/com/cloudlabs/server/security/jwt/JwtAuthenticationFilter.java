package com.cloudlabs.server.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
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

    // If got no token, pass it to the next filter and return. Otherwise, the
    // operation will continue on resulting in exception as getUsername()
    // parameter cannot be null
    if (jwt == null || jwt.isBlank()) {
      filterChain.doFilter(request, response);
      return;
    }

    String username = jwtService.getUsername(jwt);

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

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("jwt")) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }
}
