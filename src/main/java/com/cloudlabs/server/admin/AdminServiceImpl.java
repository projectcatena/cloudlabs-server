package com.cloudlabs.server.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleRepository;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.dto.UserDTO;

@Service
public class AdminServiceImpl implements AdminService{

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
            userDTO.setFullName(user.getFullname());
            userDTO.setUsername(user.getUserName());
            userDTO.setRoles(user.getRoles());
            userList.add(userDTO);
        }

        return userList;
    }
    
    public UserDTO setNewRole(UserDTO userDTO){
        Optional<User> option = userRepository.findByEmail(userDTO.getEmail());
        
        UserDTO returnUserDTO = new UserDTO();
        option.ifPresent(
            (user) -> {
            Role role = roleRepository.findByName(userDTO.getNewRole());
            if (user.getRoles().contains(role)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has this role");
            }
            List<Role> newRoleList = user.getRoles();
            newRoleList.add(role);
            user.setRoles(newRoleList);
            userRepository.save(user);

            returnUserDTO.setFullName(user.getFullname());
            returnUserDTO.setUsername(user.getUserName());
            returnUserDTO.setEmail(user.getEmail());
            returnUserDTO.setRoles(user.getRoles());

        });

        return returnUserDTO;

    }

    public UserDTO deleteRole(UserDTO userDto) {
        Optional<User> option = userRepository.findByEmail(userDto.getEmail());

        UserDTO returnDTO = new UserDTO();
        option.ifPresent((user) -> {
            try { //check if role exists && check if user has role
            Role role = roleRepository.findByName(userDto.getNewRole());
            if (user.getRoles().contains(role)) {
                user.getRoles().remove(role);
                userRepository.save(user);

                returnDTO.setEmail(user.getEmail());
                returnDTO.setFullName(user.getFullname());
                returnDTO.setUsername(user.getUserName());
                returnDTO.setRoles(user.getRoles());
            }
            else{
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not have this role");
            }
        } catch (Exception e) {
            // TODO: handle exception
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No such role");
        }
        });
        
        return returnDTO;

    }
}
