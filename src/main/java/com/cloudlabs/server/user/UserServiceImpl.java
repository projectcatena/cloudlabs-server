package com.cloudlabs.server.user;

import com.cloudlabs.server.role.ERole;
import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private Collection<? extends GrantedAuthority> authorities;

    public UserServiceImpl(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            Collection<? extends GrantedAuthority> authorities) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorities = authorities;
    }

    @Override
    public void saveUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        // encrypt the password using spring security
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        // set account roles
        setNewRole(ERole.USER, user);
        setNewRole(ERole.TUTOR, user); // remove
        setNewRole(ERole.ADMIN, user); // remove

        userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map((user) -> mapToUserDto(user))
                .collect(Collectors.toList());
    }

    private UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());

        // for admin add roles
        if (user.getRoles() != null) {
            userDto.setRoles(user.getRoles());
        }

        return userDto;
    }

    private Role checkRoleExist(ERole roleString) {
        Role role = new Role();
        role.setName(roleString);
        return roleRepository.save(role);
    }

    public Boolean setNewRole(ERole newRole, User user) {
        Role role = roleRepository.findByName(newRole);
        if (role == null) {
            role = checkRoleExist(newRole);
        }
        if (user.getRoles().contains(role)) {
            return false;
        }

        List<Role> newRoleList = user.getRoles();
        newRoleList.add(role);
        user.setRoles(newRoleList);
        userRepository.save(user);
        return true;
    }

    public void deleteRole(ERole eRole, User user) {
        try { // check if role exists && check if user has role
            Role role = roleRepository.findByName(eRole);
            if (user.getRoles().contains(role)) {
                user.getRoles().remove(role);
                userRepository.save(user);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User does not have this role");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No such role");
        }
    }

    @Override
    public String getAuthorities() {
        // Retrieve the authentication object from the SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication);
        // Retrieve the authorities for the currently authenticated user
        authorities = authentication.getAuthorities();

        // Mapping authorities
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        return roles;
    }

    public String getUsername(User user) {
        return user.getName();
    }
}
