package com.cloudlabs.server.compute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import com.cloudlabs.server.compute.dto.SourceImageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ComputeControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ComputeService computeService;

    // Since get and list require an instance to be created first, the tests for
    // get and list will all be in this specific test case
    @Test
    void createGetListThenDeleteComputeEngine_whenPublicImage() throws Exception {

        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("test-public-image");
        request.setStartupScript("");

        SourceImageDTO sourceImageDTO = new SourceImageDTO();
        sourceImageDTO.setName("debian-11");
        sourceImageDTO.setProject("debian-cloud");
        request.setSourceImage(sourceImageDTO);

        MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
        machineTypeDTO.setName("e2-micro");
        request.setMachineType(machineTypeDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ComputeDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);

        // Get list and should not be empty
        List<ComputeDTO> listComputeInstances = computeService.listComputeInstances();
        assertFalse(listComputeInstances.isEmpty());

        // Get details on one instance only, should not be empty
        ComputeDTO getComputeInstance = computeService.getComputeInstance(response.getInstanceName());
        assertNotNull(getComputeInstance);

        // Delete instance and release its public IP Address after test
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(response.getInstanceName());
        computeService.releaseStaticExternalIPAddress(
                response.getAddress().getName());

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void createThenDeleteComputeEngine_whenCustomImage() throws Exception {

        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("test-custom-image");
        request.setStartupScript("");

        SourceImageDTO sourceImageDTO = new SourceImageDTO();
        sourceImageDTO.setName("windows-server-2019");
        request.setSourceImage(sourceImageDTO);

        MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
        machineTypeDTO.setName("e2-medium");
        request.setMachineType(machineTypeDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Delete instance and release its public IP Address after test
        ComputeDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(response.getInstanceName());
        computeService.releaseStaticExternalIPAddress(
                response.getAddress().getName());

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void failCreateComputeEngine_whenIncorrectParametersGiven() throws Exception {

        String randomInstanceName = RandomStringUtils.randomAlphanumeric(10);

        ComputeDTO request = new ComputeDTO();
        request.setInstanceName(String.format("test-fail-%s", randomInstanceName));
        request.setStartupScript("");

        SourceImageDTO sourceImageDTO = new SourceImageDTO();
        sourceImageDTO.setName("debian-11");
        sourceImageDTO.setProject("debian-cloud");
        request.setSourceImage(sourceImageDTO);

        MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
        machineTypeDTO.setName("e10-micro");
        request.setMachineType(machineTypeDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // The test will create a public IP address resource, so must delete
        String ipAddressResourceName = String.format("%s-public-ip", request.getInstanceName());
        computeService.releaseStaticExternalIPAddress(ipAddressResourceName);
    }

    @Test
    void failCreateComputeEngine_whenParametersNotGiven() throws Exception {

        ComputeDTO request = new ComputeDTO();
        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void listMachineTypes_whenNoQuery() throws Exception {

        List<MachineTypeDTO> response = computeService.listMachineTypes(null);

        // Must not be empty, no query should list all machine types
        assertFalse(response.isEmpty());
    }

    @Test
    void listMachineTypes_whenQueryGiven() throws Exception {

        List<MachineTypeDTO> response = computeService.listMachineTypes("e2");

        assertThat(response)
                .extracting(MachineTypeDTO::getName)
                .anyMatch(value -> value.matches("e2-micro"));
    }

    @Test
    void createThenResetInstance() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("instance-test-1");
        request.setStartupScript("");

        SourceImageDTO sourceImageDTO = new SourceImageDTO();
        sourceImageDTO.setName("windows-server-2019");
        request.setSourceImage(sourceImageDTO);

        MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
        machineTypeDTO.setName("e2-medium");
        request.setMachineType(machineTypeDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Reset instance
        ComputeDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO resetComputeDTO = computeService.resetInstance(response.getInstanceName());

        assertNotNull(resetComputeDTO.getStatus());
    }
}
