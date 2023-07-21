package com.cloudlabs.server.admin;

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

import com.cloudlabs.server.security.auth.dto.RegisterDTO;
import com.cloudlabs.server.user.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = { "ADMIN" })
@TestInstance(Lifecycle.PER_CLASS)
public class AdminControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    void createUser_withUserCredentialsOnly() throws Exception {
        RegisterDTO request = new RegisterDTO("tester", "tester",
                "tester@gmail.com", "Pa$$w0rd");

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getUserList() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/admin/list"))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void addRole() throws Exception {
        UserDTO requestDTO = new UserDTO();
        requestDTO.setEmail("tester@gmail.com");
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
        requestDTO.setEmail("tester@gmail.com");
        requestDTO.setNewRole("user");

        String jsonString = objectMapper.writeValueAsString(requestDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/admin/delete")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }


}
