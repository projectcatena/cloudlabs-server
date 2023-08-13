package com.cloudlabs.server.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleRepository;
import com.cloudlabs.server.role.RoleType;
import com.cloudlabs.server.role.dto.RoleDTO;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.dto.UserDTO;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userList = new ArrayList<UserDTO>();
        for (User user : users) {
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(user.getEmail());
            userDTO.setFullname(user.getFullname());
            userDTO.setUsername(user.getUserName());
            userDTO.setRoles(user.getRoles()
                    .stream()
                    .map(role -> new RoleDTO(role.getName()))
                    .collect(Collectors.toList()));
            userList.add(userDTO);
        }

        return userList;
    }

    public UserDTO setNewRole(UserDTO userDTO) {
        User user = userRepository.findByEmail(userDTO.getEmail()).orElse(null);

        if (user == null) {
            return null;
        }

        Role role = roleRepository.findByName(userDTO.getNewRole());

        if (role == null) {
            if (userDTO.getNewRole().toString().toUpperCase() == "USER") {
                role = new Role(RoleType.USER);
            }
            else if (userDTO.getNewRole().toString().toUpperCase() == "TUTOR") {
                role = new Role(RoleType.TUTOR);
            }
            else {
                role = new Role(RoleType.ADMIN);
            }
            
        }

        if (user.getRoles().contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User has this role");
        }

        Set<Role> newRoles = user.getRoles();
        newRoles.add(role);

        user.setRoles(newRoles);
        userRepository.save(user);
        
        UserDTO returnUserDTO = new UserDTO();
        returnUserDTO.setFullname(user.getFullname());
        returnUserDTO.setUsername(user.getUserName());
        returnUserDTO.setEmail(user.getEmail());
        returnUserDTO.setRoles(
                user.getRoles()
                        .stream()
                        .map(userRole -> new RoleDTO(userRole.getName()))
                        .collect(Collectors.toList()));

        return returnUserDTO;
    }

    public UserDTO deleteRole(UserDTO userDto) {
        User user = userRepository.findByEmail(userDto.getEmail()).orElse(null);
        Role role = roleRepository.findByName(userDto.getNewRole());

        if (user == null || role == null) {
            return null;
        }

        UserDTO returnDTO = new UserDTO();
        if (user.getRoles().contains(role)) {
            user.getRoles().remove(role);
            userRepository.save(user);

            returnDTO.setEmail(user.getEmail());
            returnDTO.setFullname(user.getFullname());
            returnDTO.setUsername(user.getUserName());
            returnDTO.setRoles(
                    user.getRoles()
                            .stream()
                            .map(userRole -> new RoleDTO(userRole.getName()))
                            .collect(Collectors.toList()));
        } else {
            return null;
        }

        return returnDTO;
    }
}
