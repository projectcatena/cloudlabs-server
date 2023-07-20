package com.cloudlabs.server.user;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloudlabs.server.user.dto.UserDTO;

@Service
public class UserServiceImpl implements UserService{
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO getUserDetails(Long id) {
        Optional<User> option = userRepository.findById(id);

        UserDTO userDTO = new UserDTO();
        option.ifPresent((user) -> {
            userDTO.setEmail(user.getEmail());
            userDTO.setFullName(user.getFullname());
            userDTO.setUsername(user.getUserName());
        });

        return userDTO;
    }

    public UserDTO updateUserDetails(UserDTO userDTO) {
        Optional<User> option = userRepository.findByEmail(userDTO.getEmail());

        UserDTO result = new UserDTO();
        option.ifPresent((user) -> {
            if (userDTO.getCurrentPassword() == null) { // no password change
                user.setEmail(userDTO.getEmail());
                user.setFullname(userDTO.getFullName());
                user.setUsername(userDTO.getUsername());
            }
            else { // password change
                if (passwordEncoder.matches(userDTO.getCurrentPassword(), user.getPassword())) { // wrong password
                    user.setEmail(userDTO.getEmail());
                    user.setFullname(userDTO.getFullName());
                    user.setUsername(userDTO.getUsername());
                    user.setPassword(passwordEncoder.encode(userDTO.getNewPassword())); // set new password
                    
                }
                else {
                    throw new Error("Wrong password");
                }
            }
            userRepository.save(user);
            userDTO.setEmail(user.getEmail());
            userDTO.setFullName(user.getFullname());
            userDTO.setUsername(user.getUsername());
        });

        return result;
    }
}
