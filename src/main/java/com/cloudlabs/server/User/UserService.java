package com.cloudlabs.server.user;

import java.util.List;

public interface UserService {
    void saveUser(UserDto user);

    User findByEmail(String email);

    List<UserDto> findAllUsers();

    String getAuthorities();

    String getUsername(User user);
}