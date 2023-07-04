package com.cloudlabs.server.user;

import java.util.List;

import com.cloudlabs.server.role.ERole;

public interface UserService {
    void saveUser(UserDto user);

    User findByEmail(String email);

    List<UserDto> findAllUsers();

    String getAuthorities();

    String getUsername(User user);

    Boolean setNewRole(ERole eRole, User user);

    void deleteRole(ERole eRole, User user);
}