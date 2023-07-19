package com.cloudlabs.server.compute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import com.cloudlabs.server.compute.dto.SourceImageDTO;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.dto.UserDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(username = "tutor", roles = { "TUTOR" })
public class ComputeControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ComputeService computeService;

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private UserRepository userRepository;

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
    @WithMockUser(username = "user", roles = { "USER" })
    void failCreateComputeEngine_whenPublicImageAndNormalUserRole()
            throws Exception {

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

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
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
    void createThenResetInstanceThenDelete() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("instance-test-reset");
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

        ComputeDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        JsonNode jsonNode = objectMapper.createObjectNode().put(
                "instanceName", response.getInstanceName());
        String jsonString2 = objectMapper.writeValueAsString(jsonNode);

        // Reset instance
        mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString2))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Delete instance and release its public IP Address after test
        ComputeDTO deleteresponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(deleteresponse.getInstanceName());
        computeService.releaseStaticExternalIPAddress(
                deleteresponse.getAddress().getName());

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void createThenGetInstanceStatusThenDelete() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("instance-test-status");
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

        ComputeDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        JsonNode jsonNode = objectMapper.createObjectNode().put(
                "instanceName", response.getInstanceName());
        String jsonString2 = objectMapper.writeValueAsString(jsonNode);

        // Get instance status
        mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString2))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Delete instance and release its public IP Address after test
        ComputeDTO deleteresponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(deleteresponse.getInstanceName());
        computeService.releaseStaticExternalIPAddress(
                deleteresponse.getAddress().getName());

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void createThenStopInstanceThenDelete() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("instance-test-stop");
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

        ComputeDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        JsonNode jsonNode = objectMapper.createObjectNode().put(
                "instanceName", response.getInstanceName());
        String jsonString2 = objectMapper.writeValueAsString(jsonNode);

        // Stop instance
        mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/stop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString2))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Delete instance and release its public IP Address after test
        ComputeDTO deleteresponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(deleteresponse.getInstanceName());
        computeService.releaseStaticExternalIPAddress(
                deleteresponse.getAddress().getName());

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void createThenStopThenStartInstanceThenDelete() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("instance-test-start");
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

        // Stop instance
        ComputeDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO resetComputeDTO = computeService.stopInstance(response.getInstanceName());

        assertNotNull(resetComputeDTO.getStatus());

        ComputeDTO response2 = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        JsonNode jsonNode = objectMapper.createObjectNode().put(
                "instanceName", response2.getInstanceName());
        String jsonString2 = objectMapper.writeValueAsString(jsonNode);

        // Start instance
        mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString2))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Delete instance and release its public IP Address after test
        ComputeDTO deleteresponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(deleteresponse.getInstanceName());
        computeService.releaseStaticExternalIPAddress(
                deleteresponse.getAddress().getName());

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void failResetInstance_whenInvalidParametersGiven() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("instance-test-reset-fail");
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

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Invalid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        // Delete instance and release its public IP Address after test
        ComputeDTO deleteresponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(deleteresponse.getInstanceName());
        computeService.releaseStaticExternalIPAddress(
                deleteresponse.getAddress().getName());

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void failGetInstanceStatus_whenInvalidParametersGiven() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("instance-test-status-fail");
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

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Invalid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        // Delete instance and release its public IP Address after test
        ComputeDTO deleteresponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(deleteresponse.getInstanceName());
        computeService.releaseStaticExternalIPAddress(
                deleteresponse.getAddress().getName());

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void failStopInstance_whenInvalidParametersGiven() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("instance-test-stop-fail");
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

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/stop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Invalid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        // Delete instance and release its public IP Address after test
        ComputeDTO deleteresponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(deleteresponse.getInstanceName());
        computeService.releaseStaticExternalIPAddress(
                deleteresponse.getAddress().getName());

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void failStartInstance_whenInvalidParametersGiven() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("instance-test-start-fail");
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

        // Stop instance
        ComputeDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO resetComputeDTO = computeService.stopInstance(response.getInstanceName());

        assertNotNull(resetComputeDTO.getStatus());

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Invalid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        // Delete instance and release its public IP Address after test
        ComputeDTO deleteresponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), ComputeDTO.class);
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(deleteresponse.getInstanceName());
        computeService.releaseStaticExternalIPAddress(
                deleteresponse.getAddress().getName());

        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void addComputeInstanceUsers_whenValidParametersGiven() throws Exception {
        // Create an entry in database will do, as this doesn't need GCP to test
        Compute compute = new Compute("mock-entry", "e2-micro", "10.10.1.1");
        computeRepository.save(compute);

        // Create new test user
        User user = new User();
        user.setUsername("test3");
        user.setFullname("Bob");
        user.setEmail("test3@gmail.com");
        user.setPassword("test@123");
        userRepository.save(user);

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test3@gmail.com");
        List<UserDTO> userDTOs = Arrays.asList(userDTO);

        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("mock-entry");
        request.setUsers(userDTOs);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/add-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        assertTrue(computeRepository
                .findByUsers_EmailAndInstanceName(userDTO.getEmail(),
                        compute.getInstanceName())
                .isPresent());

        // Clean up
        computeRepository.deleteByInstanceName(request.getInstanceName());
        userRepository.deleteByEmail(userDTO.getEmail());
    }

    @Test
    void removeComputeInstanceUsers_whenValidParametersGiven() throws Exception {

        User user = new User();
        user.setUsername("test4");
        user.setFullname("John");
        user.setEmail("test4@gmail.com");
        user.setPassword("test@123");

        Set<User> users = new HashSet<>();
        users.add(user);

        // Create an entry in database will do, as this doesn't need GCP to test
        Compute compute = new Compute("mock-entry-remove", "e2-micro", "10.10.1.1", users);
        computeRepository.save(compute);

        assertNotNull(computeRepository.findByUsers_EmailAndInstanceName(
                user.getEmail(), compute.getInstanceName()));

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test4@gmail.com");
        List<UserDTO> userDTOs = Arrays.asList(userDTO);

        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("mock-entry-remove");
        request.setUsers(userDTOs);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/remove-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        assertTrue(computeRepository
                .findByUsers_EmailAndInstanceName(userDTO.getEmail(),
                        request.getInstanceName())
                .isEmpty());

        // Clean up
        computeRepository.deleteByInstanceName(request.getInstanceName());
        userRepository.deleteByEmail(userDTO.getEmail());
    }
}
