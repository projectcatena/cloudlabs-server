package com.cloudlabs.server.admin;

import java.util.List;

import com.cloudlabs.server.user.dto.UserDTO;

public interface AdminService {
    
    List<UserDTO> getAllUsers();

    UserDTO setNewRole(UserDTO userDTO);

    UserDTO deleteRole(UserDTO userDto);
}
