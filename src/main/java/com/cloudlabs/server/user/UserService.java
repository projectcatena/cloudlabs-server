package com.cloudlabs.server.user;

import com.cloudlabs.server.user.dto.UserDTO;

public interface UserService {
    
    UserDTO getUserDetails(Long id);

    UserDTO updateUserDetails(UserDTO userDTO);

    Boolean deleteUser(UserDTO userDTO);
}
