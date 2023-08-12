package com.cloudlabs.server.user;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleRepository;
import com.cloudlabs.server.role.RoleType;
import com.cloudlabs.server.security.auth.dto.LoginDTO;
import com.cloudlabs.server.user.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@WithMockUser(username = "tutor", roles = { "ADMIN", "USER" })
public class UserControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeAll
    void setup() {
        User user = userRepository.findByEmail("yalwa@gmail.com").orElse(null);
                if (user == null) {
                        Role tutorRole = roleRepository.findByName(RoleType.TUTOR);
                        Role adminRole = roleRepository.findByName(RoleType.ADMIN);
                        if (tutorRole == null) {
                                tutorRole = new Role(RoleType.TUTOR);
                        }
                        else if (adminRole == null) {
                                adminRole = new Role(RoleType.ADMIN);
                        }
                        Set<Role> roles = new HashSet<>(Arrays.asList(tutorRole,adminRole));
                        user = new User("Bobby", "tutor", "yalwa@gmail.com", passwordEncoder.encode("Pa$$w0rd"));
                        userRepository.save(user);
                        user.setRoles(roles);
                        userRepository.save(user);
                }

    }

    @AfterAll
    void teardown() {
        userRepository.deleteByEmail("yalwa@gmail.com");
        roleRepository.delete(new Role(RoleType.USER));
        roleRepository.delete(new Role(RoleType.ADMIN));
    }

    @Test
    @WithUserDetails(value = "yalwa@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void retrieveUserDetails() throws Exception {
        User user = userRepository.findByEmail("yalwa@gmail.com").get();

        this.mockMvc
                .perform(
                        MockMvcRequestBuilders.get("/user/" + user.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithUserDetails(value = "yalwa@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateUserDetails() throws Exception {

        UserDTO userDTO = new UserDTO();
        userDTO.setFullname("test");
        userDTO.setUsername("test");
        userDTO.setEmail("yalwa@gmail.com");
        userDTO.setCurrentPassword("Pa$$w0rd");
        userDTO.setNewPassword("Test@123");

        String jsonString = objectMapper.writeValueAsString(userDTO);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());

        LoginDTO loginDTO = new LoginDTO("yalwa@gmail.com", "Test@123");

        String loginJsonString = objectMapper.writeValueAsString(loginDTO);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithUserDetails(value = "yalwa@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteUser() throws Exception {
        // create user to be deleted
        User user = userRepository.findByEmail("delete@gmail.com").orElse(null);
                if (user == null) {
                        Role tutorRole = roleRepository.findByName(RoleType.TUTOR);
                        if (tutorRole == null) {
                                tutorRole = new Role(RoleType.TUTOR);
                        }
                        Set<Role> roles = new HashSet<>(Arrays.asList(tutorRole));
                        user = new User("delete", "delete", "delete@gmail.com", passwordEncoder.encode("Pa$$w0rd"));
                        userRepository.save(user);
                        user.setRoles(roles);
                        userRepository.save(user);
                }

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("delete@gmail.com");

        String jsonString = objectMapper.writeValueAsString(userDTO);

        this.mockMvc
                .perform(MockMvcRequestBuilders.delete("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // try to log in as deleted user
        LoginDTO loginDTO = new LoginDTO("delete@gmail.com", "Pa$$w0rd");

        String loginJsonString = objectMapper.writeValueAsString(loginDTO);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJsonString))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
