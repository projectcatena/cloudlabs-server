package com.cloudlabs.server.compute;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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

import com.cloudlabs.server.subnet.Subnet;
import com.cloudlabs.server.subnet.SubnetRepository;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
public class ComputeRepositoryTests {

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubnetRepository subnetRepository;

    @BeforeAll
    void setup() {

        Set<User> users = new HashSet<>();

        User user = new User();
        user.setUsername("computeRepo");
        user.setFullname("Bob");
        user.setEmail("computerepo@gmail.com");
        // encrypt the password using spring security
        user.setPassword("test@123");

        users.add(user);

        // Make a valid 2nd user to query
        User user2 = new User();
        user2.setUsername("computeRepo2");
        user2.setFullname("John");
        user2.setEmail("computerepo2@gmail.com");
        // encrypt the password using spring security
        user2.setPassword("test@123");
        userRepository.save(user2);

        Subnet subnet = subnetRepository.findBySubnetName("test-subnet-compute-repo");

        if (subnet == null) {
            // Pre-configured on GCP
            subnet = subnetRepository.save(
                    new Subnet("test-subnet-compute-repo", "10.254.3.0/24"));
        }

        Compute compute = new Compute("test", "e2-micro", "10.254.3.2", users, subnet);
        computeRepository.save(compute);
    }

    @AfterAll
    void cleanup() {
        computeRepository.deleteByInstanceName("test");
        User user1 = userRepository.findByEmail("computerepo@gmail.com").get();
        User user2 = userRepository.findByEmail("computerepo2@gmail.com").get();
        userRepository.deleteAll(Arrays.asList(user1, user2));
        subnetRepository.deleteBySubnetName("test-subnet-compute-repo");
    }

    @Test
    void listUserComputeInstances_whenEmailGiven() {
        List<Compute> computeInstances = computeRepository.findByUsers_Email("computerepo@gmail.com");

        assertFalse(computeInstances.isEmpty());
        assertThat(computeInstances)
                .extracting(Compute::getUsers)
                .anySatisfy(users -> assertThat(users)
                        .extracting(User::getEmail)
                        .anyMatch(value -> value.matches("computerepo@gmail.com")));
    }

    @Test
    void findCompute_whenInstanceNameGiven() {
        assertThat(computeRepository.findByInstanceName("test").isPresent());
    }

    @Test
    void findCompute_whenUserAndInstanceNameGiven() {
        assertThat(computeRepository
                .findByUsers_EmailAndInstanceName("computerepo@gmail.com", "test")
                .isPresent());
    }

    @Test
    void emptyListUserComputeInstances_whenEmailGivenAndUserIsNotAssignedAnyComputeInstances() {
        List<Compute> computeInstances = computeRepository.findByUsers_Email("computerepo2@gmail.com");

        assertThat(computeInstances.isEmpty());
    }

    @Test
    void failListUserComputeInstances_whenUserDoesNotExist() {
        List<Compute> computeInstances = computeRepository.findByUsers_Email("not-exist-user@gmail.com");

        assertThat(computeInstances.isEmpty());
    }
}
