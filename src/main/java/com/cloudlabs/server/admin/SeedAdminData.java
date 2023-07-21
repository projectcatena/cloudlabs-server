package com.cloudlabs.server.admin;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleType;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;

@Component
public class SeedAdminData implements CommandLineRunner{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedData();
    }

    private void seedData() {
        if (userRepository.count() == 0) {
            Role adminRole = new Role(RoleType.ADMIN);
            Role tutorRole = new Role(RoleType.TUTOR);
            Role userRole = new Role(RoleType.USER);
            List<Role> roles = Arrays.asList(adminRole,tutorRole,userRole);
            User adminUser = new User(
                "admin",
                "admin",
                "admin@gmail.com",
                passwordEncoder.encode("admin"), // change password upon login
                roles);
            
            userRepository.save(adminUser);
        }
    }
    
}
