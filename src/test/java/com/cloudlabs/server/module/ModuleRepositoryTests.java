package com.cloudlabs.server.module;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
public class ModuleRepositoryTests {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    void setup() {

        Set<User> users = new HashSet<>();

        User user = new User();
        user.setUsername("moduleRepo");
        user.setFullname("Bob");
        user.setEmail("modulerepo@gmail.com");
        // encrypt the password using spring security
        user.setPassword("test@123");

        users.add(user);

        // Make a valid 2nd user to query
        User user2 = new User();
        user2.setUsername("moduleRepo2");
        user2.setFullname("John");
        user2.setEmail("modulerepo2@gmail.com");
        // encrypt the password using spring security
        user2.setPassword("test@123");
        userRepository.save(user2);

        Module module = new Module("Subtitle", "Name", "Description", users);
        moduleRepository.save(module);

        assertNotNull(moduleRepository.findById(module.getModuleId()));
    }

    @AfterAll
    void deleteModuleByModuleId() {
        moduleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void listModuleForUser_whenUserEmailIsGiven() {
        List<Module> modules = moduleRepository.findByUsers_Email("modulerepo@gmail.com");

        assertFalse(modules.isEmpty());
        assertThat(modules)
                .extracting(Module::getUsers)
                .anySatisfy(users -> assertThat(users)
                        .extracting(User::getEmail)
                        .anyMatch(value -> value.matches("modulerepo@gmail.com")));
    }
}
