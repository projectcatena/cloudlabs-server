package com.cloudlabs.server.security;

import org.springframework.security.core.userdetails.UserDetails;

import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserDto;

public interface AuthService {
    String createJwtToken(String email, User user, UserDetails userDetails);

    UserDto createNewUser(String name, String email, String password);
}
