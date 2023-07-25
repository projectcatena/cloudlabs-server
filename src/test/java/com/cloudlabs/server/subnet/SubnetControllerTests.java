package com.cloudlabs.server.subnet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cloudlabs.server.subnet.dto.SubnetDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(username = "tutor", roles = { "TUTOR" })
public class SubnetControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SubnetService subnetService;

    @Test
    void createThenDeleteSubnet_whenCorrectParametersGiven() throws Exception {

        SubnetDTO request = new SubnetDTO();
        request.setSubnetName("test-subnet-1");
        request.setIpv4Range("10.10.2.0/24");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/network/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        SubnetDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), SubnetDTO.class);

        // Delete subnet
        SubnetDTO deleteSubnetDTO = subnetService.deleteSubnet(response.getSubnetName());
        assertNotNull(deleteSubnetDTO.getStatus());
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    void failCreateSubnet_whenNormalUserRole() throws Exception {

        SubnetDTO request = new SubnetDTO();
        request.setSubnetName("fail-test-subnet-1");
        request.setIpv4Range("10.10.2.0/24");

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/network/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();

    }

    @Test
    void failCreateSubnet_whenParametersNotGiven() throws Exception {

        SubnetDTO request = new SubnetDTO();
        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/network/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
                
    }

    @Test
    void deleteSubnet_whenCorrectParametersGiven() throws Exception {
        SubnetDTO request = new SubnetDTO();
        request.setSubnetName("test-subnet-2");
        request.setIpv4Range("10.10.2.0/24");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/network/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        SubnetDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), SubnetDTO.class);

        // Delete subnet test
        SubnetDTO delete_request = new SubnetDTO();
        delete_request.setSubnetName(response.getSubnetName());

        String jsonString2 = objectMapper.writeValueAsString(delete_request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/network/delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString2))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();    
    }
    
}
