package com.cloudlabs.server.compute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cloudlabs.server.user.User;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ComputeRepositoryTests {

    @Autowired
    private ComputeRepository computeRepository;

    /*
     * @BeforeEach
     * void setup() {
     *
     * List<User> users = new ArrayList<>();
     *
     * User user = new User();
     * user.setName("test");
     * user.setEmail("test@gmail.com");
     * // encrypt the password using spring security
     * user.setPassword("test@123");
     *
     * users.add(user);
     *
     * User user2 = new User();
     * user2.setName("test2");
     * user2.setEmail("test2@gmail.com");
     * // encrypt the password using spring security
     * user2.setPassword("test@123");
     *
     * users.add(user);
     *
     * Compute compute = new Compute("test", "e2-micro", "10.10.1.1", users);
     * computeRepository.save(compute);
     * }
     */

    @Test
    void createCompute_whenInstanceDetailsGiven() {

        List<User> users = new ArrayList<>();

        User user = new User();
        user.setName("test");
        user.setEmail("test@gmail.com");
        // encrypt the password using spring security
        user.setPassword("test@123");

        users.add(user);

        User user2 = new User();
        user2.setName("test2");
        user2.setEmail("test2@gmail.com");
        // encrypt the password using spring security
        user2.setPassword("test@123");

        users.add(user);

        Compute compute = new Compute("test", "e2-micro", "10.10.1.1", users);
        computeRepository.save(compute);
    }

    @Test
    void findCompute_whenInstanceNameGiven() {
        Compute compute = computeRepository.findByInstanceName("test");
        assertNotNull(compute);
    }

    @Test
    void deleteComputeInstance_whenInstanceNameGiven() {
        // Compute compute =
        // computeRepository.findByInstanceName("windows-server-2019");
        computeRepository.deleteByInstanceName("test");
        // computeRepository.delete(compute);
    }

    @Test
    void listUserComputeInstances_whenEmailGiven() {
        List<Compute> computeInstances = computeRepository.findByUsers_Email("test@gmail.com");

        assertNotNull(computeInstances);
        assertThat(computeInstances)
                .extracting(Compute::getUsers)
                .anySatisfy(users -> assertThat(users)
                        .extracting(User::getEmail)
                        .anyMatch(value -> value.matches("test@gmail.com")));
    }
}
