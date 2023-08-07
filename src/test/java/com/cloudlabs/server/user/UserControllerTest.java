package com.cloudlabs.server.user;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.jdbc.JdbcTestUtils;
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
@WithMockUser(username = "tester", roles = { "USER" })
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
    void setup(@Autowired JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_roles");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "user_table");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "roles");
        Role role = roleRepository.findByName(RoleType.USER);

        if (role == null) {
                role = new Role(RoleType.USER);
        }

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User mock_user = new User("tester", "tester", "tester@gmail.com", passwordEncoder.encode("Pa$$w0rd"));
        mock_user.setRoles(roles);
        userRepository.save(mock_user);

    }

    @AfterAll
    void teardown() {
        userRepository.deleteByEmail("tester@gmail.com");
        roleRepository.delete(new Role(RoleType.USER));
    }

    @Test
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void retrieveUserDetails() throws Exception {
        User user = userRepository.findByEmail("tester@gmail.com").get();

        this.mockMvc
                .perform(
                        MockMvcRequestBuilders.get("/user/" + user.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateUserDetails() throws Exception {

        UserDTO userDTO = new UserDTO();
        userDTO.setFullname("test");
        userDTO.setUsername("test");
        userDTO.setEmail("tester@gmail.com");
        userDTO.setCurrentPassword("Pa$$w0rd");
        userDTO.setNewPassword("Test@123");

        String jsonString = objectMapper.writeValueAsString(userDTO);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());

        LoginDTO loginDTO = new LoginDTO("tester@gmail.com", "Test@123");

        String loginJsonString = objectMapper.writeValueAsString(loginDTO);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
