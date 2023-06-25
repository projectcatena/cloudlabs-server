package com.cloudlabs.server.user;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloudlabs.server.role.ERole;
import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleRepository;

@Service
public class UserServiceImpl implements UserService  {

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

        setNewRole(ERole.USER, user);
        setNewRole(ERole.TUTOR, user);
        
        //Test
        System.out.println(user.getRoles().get(0));
        System.out.println(user.getRoles().get(1));
        
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

    private UserDto mapToUserDto(User user){
        UserDto userDto = new UserDto();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    private Role checkRoleExist(ERole roleString){
        Role role = new Role();
        role.setName(roleString);
        return roleRepository.save(role);
    }

    private void setNewRole(ERole newRole, User user){
        Role role = roleRepository.findByName(newRole);
        if(role == null){
            role = checkRoleExist(newRole);
        }
        List<Role> newRoleList = user.getRoles();
        newRoleList.add(role);
        user.setRoles(newRoleList);

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