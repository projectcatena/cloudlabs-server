package com.cloudlabs.server.admin;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleRepository;
import com.cloudlabs.server.role.RoleType;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = { "ADMIN" })
@TestInstance(Lifecycle.PER_CLASS)
public class AdminControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    void setup() throws Exception {
        User user = userRepository.findByEmail("administrator@gmail.com").orElse(null);
            if (user == null) {
                Role adminRole = roleRepository.findByName(RoleType.ADMIN);
                Role tutorRole = null;
                Role userRole = null;
                if (adminRole == null) {
                    adminRole = new Role(RoleType.ADMIN);
                    tutorRole = new Role(RoleType.TUTOR);
                    userRole = new Role(RoleType.USER);
                }
                Set<Role> roles = new HashSet<>(Arrays.asList(adminRole, tutorRole, userRole));
                user = new User("Bobby", "administrator", "administrator@gmail.com", "Pa$$w0rd");
                userRepository.save(user);
                user.setRoles(roles);
                userRepository.save(user);
            }
    }

    @AfterAll
    void teardown() throws Exception {
        userRepository.deleteByEmail("administrator@gmail.com");
    }

    @Test
    void getUserList() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/admin/list"))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void addRole() throws Exception {
        UserDTO requestDTO = new UserDTO();
        requestDTO.setEmail("administrator@gmail.com");
        requestDTO.setNewRole("tutor");

        String jsonString = objectMapper.writeValueAsString(requestDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/admin/add")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString))
        .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    void deleteRole() throws Exception{
        UserDTO requestDTO = new UserDTO();
        requestDTO.setEmail("administrator@gmail.com");
        requestDTO.setNewRole("tutor");

        String jsonString = objectMapper.writeValueAsString(requestDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/admin/delete")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }


}
