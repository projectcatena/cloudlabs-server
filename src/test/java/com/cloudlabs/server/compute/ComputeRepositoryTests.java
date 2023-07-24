package com.cloudlabs.server.compute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import java.util.Arrays;
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
public class ComputeRepositoryTests {

    @Autowired
    private ComputeRepository computeRepository;

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

        Compute compute = new Compute("test", "e2-micro", "10.10.1.1", users);
        computeRepository.save(compute);
    }

    @AfterAll
    void cleanup() {
        // Compute compute =
        // computeRepository.findByInstanceName("windows-server-2019");
        computeRepository.deleteByInstanceName("test");
        // computeRepository.delete(compute);
        User user1 = userRepository.findByEmail("test@gmail.com").get();
        User user2 = userRepository.findByEmail("test2@gmail.com").get();
        userRepository.deleteAll(Arrays.asList(user1, user2));
    }

    @Test
    void listUserComputeInstances_whenEmailGiven() {
        List<Compute> computeInstances = computeRepository.findByUsers_Email("test@gmail.com");

        assertFalse(computeInstances.isEmpty());
        assertThat(computeInstances)
                .extracting(Compute::getUsers)
                .anySatisfy(users -> assertThat(users)
                        .extracting(User::getEmail)
                        .anyMatch(value -> value.matches("test@gmail.com")));
    }

    @Test
    void findCompute_whenInstanceNameGiven() {
        assertThat(computeRepository.findByInstanceName("test").isPresent());
    }

    @Test
    void findCompute_whenUserAndInstanceNameGiven() {
        assertThat(computeRepository
                .findByUsers_EmailAndInstanceName("test@gmail.com", "test")
                .isPresent());
    }

    @Test
    void emptyListUserComputeInstances_whenEmailGivenAndUserIsNotAssignedAnyComputeInstances() {
        List<Compute> computeInstances = computeRepository.findByUsers_Email("test2@gmail.com");

        assertThat(computeInstances.isEmpty());
    }

    @Test
    void failListUserComputeInstances_whenUserDoesNotExist() {
        List<Compute> computeInstances = computeRepository.findByUsers_Email("not-exist-user@gmail.com");

        assertThat(computeInstances.isEmpty());
    }
}
