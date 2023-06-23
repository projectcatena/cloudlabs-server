package com.cloudlabs.server.compute;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ComputeControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ComputeService computeService;

    @Test
    void createComputeEngine_whenParametersGiven() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/compute/create")
            .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                        \"instanceName\": \"test\",
                        \"script\": \"\",
                        \"sourceImage\": {
                            \"name\": \"debian-11\",
                            \"project\": \"debian-cloud\"
                        },
                        \"machineType\": {
                            \"name\": \"e2-micro\"
                        }
                    }
                    """))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        ComputeDTO deleteComputeDTO = computeService.deleteInstance("test");

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void failCreateComputeEngine_whenIncorrectParametersGiven() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/compute/create")
            .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                        \"instanceName\": \"test\",
                        \"script\": \"\",
                        \"sourceImage\": {
                            \"name\": \"debian-11\",
                            \"project\": \"debian-cloud\"
                        },
                        \"machineType\": {
                            \"name\": \"e10-micro\"
                        }
                    }
                    """))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void failCreateComputeEngine_whenParametersNotGiven() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/compute/create")
            .contentType(MediaType.APPLICATION_JSON).content("""
                    {

                    }
                    """))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
