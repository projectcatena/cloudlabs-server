package com.cloudlabs.server.module;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
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
        user.setUsername("test");
        user.setFullname("Bob");
        user.setEmail("test@gmail.com");
        // encrypt the password using spring security
        user.setPassword("test@123");

        users.add(user);

        // Make a valid 2nd user to query
        User user2 = new User();
        user2.setUsername("test2");
        user2.setFullname("John");
        user2.setEmail("test2@gmail.com");
        // encrypt the password using spring security
        user2.setPassword("test@123");
        userRepository.save(user2);

        Module module = new Module("Subtitle", "Name", "Description", users);
        moduleRepository.save(module);

        assertNotNull(moduleRepository.findByModuleId(module.getModuleId()));
    }

    @AfterAll
    void deleteModuleByModuleId() {
        moduleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void listModuleForUser_whenUserEmailIsGiven() {
        List<Module> modules = moduleRepository.findByUsers_Email("test@gmail.com");

        assertFalse(modules.isEmpty());
        assertThat(modules)
                .extracting(Module::getUsers)
                .anySatisfy(users -> assertThat(users)
                        .extracting(User::getEmail)
                        .anyMatch(value -> value.matches("test@gmail.com")));
    }
}
