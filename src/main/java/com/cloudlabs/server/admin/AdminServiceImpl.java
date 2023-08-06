package com.cloudlabs.server.admin;

import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleRepository;
import com.cloudlabs.server.role.dto.RoleDTO;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.dto.UserDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        Optional<User> option = userRepository.findByEmail(userDTO.getEmail());

        UserDTO returnUserDTO = new UserDTO();
        option.ifPresent((user) -> {
            Role role = roleRepository.findByName(userDTO.getNewRole());
            if (user.getRoles().contains(role)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User has this role");
            }
            Set<Role> newRoles = user.getRoles();
            newRoles.add(role);

            user.setRoles(newRoles);
            userRepository.save(user);

            returnUserDTO.setFullname(user.getFullname());
            returnUserDTO.setUsername(user.getUserName());
            returnUserDTO.setEmail(user.getEmail());
            returnUserDTO.setRoles(
                    user.getRoles()
                            .stream()
                            .map(userRole -> new RoleDTO(userRole.getName()))
                            .collect(Collectors.toList()));
        });

        return returnUserDTO;
    }

    public UserDTO deleteRole(UserDTO userDto) {
        Optional<User> option = userRepository.findByEmail(userDto.getEmail());

        UserDTO returnDTO = new UserDTO();
        option.ifPresent((user) -> {
            try { // check if role exists && check if user has role
                Role role = roleRepository.findByName(userDto.getNewRole());
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
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "User does not have this role");
                }
            } catch (Exception e) {
                // TODO: handle exception
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No such role");
            }
        });

        return returnDTO;
    }
}
