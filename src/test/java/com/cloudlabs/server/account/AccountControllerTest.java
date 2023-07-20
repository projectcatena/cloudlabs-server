package com.cloudlabs.server.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.cloudlabs.server.security.auth.dto.LoginDTO;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@WithMockUser(username = "tester", roles = { "USER" })
public class AccountControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void retrieveUserDetails() throws Exception {
        User mock_user = new User("tester", "tester", "tester@gmail.com", passwordEncoder.encode("Pa$$w0rd"));
        userRepository.save(mock_user);

        Long id = Long.valueOf(1);

        this.mockMvc
            .perform(MockMvcRequestBuilders.get("/account/get/" + id))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void updateUserDetails() throws Exception {
        User mock_user = new User("tester2", "tester2", "tester2@gmail.com", passwordEncoder.encode("Pa$$w0rd"));
        userRepository.save(mock_user);

        UserDTO userDTO = new UserDTO();
        userDTO.setFullName("test2");
        userDTO.setUsername("test2");
        userDTO.setEmail("tester2@gmail.com");
        userDTO.setCurrentPassword("Pa$$w0rd");
        userDTO.setNewPassword("Test@123");

        String jsonString = objectMapper.writeValueAsString(userDTO);

        this.mockMvc
            .perform(MockMvcRequestBuilders.post("/account/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isOk());

        LoginDTO loginDTO = new LoginDTO("tester2@gmail.com", "Test@123");

        String loginJsonString = objectMapper.writeValueAsString(loginDTO);

        this.mockMvc
            .perform(MockMvcRequestBuilders.post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginJsonString))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
