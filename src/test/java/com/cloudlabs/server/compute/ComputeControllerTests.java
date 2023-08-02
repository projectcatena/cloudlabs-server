package com.cloudlabs.server.compute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudlabs.server.compute.dto.AddressDTO;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import com.cloudlabs.server.compute.dto.SourceImageDTO;
import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleRepository;
import com.cloudlabs.server.role.RoleType;
import com.cloudlabs.server.subnet.Subnet;
import com.cloudlabs.server.subnet.SubnetRepository;
import com.cloudlabs.server.subnet.SubnetService;
import com.cloudlabs.server.subnet.dto.SubnetDTO;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(username = "tutor@gmail.com", roles = { "TUTOR" })
@TestInstance(Lifecycle.PER_CLASS)
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

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SubnetRepository subnetRepository;

    @Autowired
    private SubnetService subnetService;

    // Mock auth services

    @BeforeAll
    public void setup() throws Exception {
        // After getting from DB, will be detached
        Role role = roleRepository.findByName(RoleType.TUTOR);

        if (role == null) {
            role = new Role(RoleType.TUTOR);
        }

        // If existing role is saved together with user, Hibernate will persist both
        // and assign a new generated ID, instead of updating/merging the existing
        // role, and then flush to db. As such, by doing this, since the role
        // already exists, there will be a detached entity passed to persist error,
        // as hibernate do not want to generate and save the role as a new record in
        // database.
        // https://thorben-janssen.com/persist-save-merge-saveorupdate-whats-difference-one-use/
        Set<Role> roles = new HashSet<>(Arrays.asList(role));
        User user = new User("ComputeTutor", "computeTutor",
                "computetutor@gmail.com", "Pa$$w0rd");
        userRepository.save(user);

        // Hence, must save role seperately if the role already exists. By doing
        // this, the user is already in the persistance context, and the role will
        // be updated with the user entity instead of persisted.
        // https://stackoverflow.com/questions/31037503/spring-data-jpa-detached-entity
        user.setRoles(roles);
        userRepository.save(user);

        SubnetDTO request = new SubnetDTO();
        request.setSubnetName("test-subnet-compute");
        request.setIpv4Range("10.254.2.0/24");
        subnetService.createSubnet(request);
    }

    @AfterAll
    void teardown() throws Exception {
        userRepository.deleteByEmail("computetutor@gmail.com");
        subnetRepository.deleteBySubnetName("test-subnet-compute");
        subnetService.deleteSubnet("test-subnet-compute");
    }

    // Since get and list require an instance to be created first, the tests for
    // get and list will all be in this specific test case
    @Test
    @WithUserDetails(value = "computetutor@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setSubnetName("test-subnet-compute");
        request.setAddress(addressDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // List
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/compute/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());

        // Cleanup
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithUserDetails(value = "computetutor@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getComputeInstance_whenAuthenticatedAndAfterCreation() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("test-get-instance");
        request.setStartupScript("");

        SourceImageDTO sourceImageDTO = new SourceImageDTO();
        sourceImageDTO.setName("debian-11");
        sourceImageDTO.setProject("debian-cloud");
        request.setSourceImage(sourceImageDTO);

        MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
        machineTypeDTO.setName("e2-micro");
        request.setMachineType(machineTypeDTO);

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setSubnetName("test-subnet-compute");
        request.setAddress(addressDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());

        this.mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/compute/instance?instanceName=test-get-instance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.instanceName").isNotEmpty());

        // Cleanup
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithUserDetails(value = "computetutor@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setSubnetName("test-subnet-compute");
        request.setAddress(addressDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Cleanup
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createLimitThenDeleteComputeEngine_whenPublicImage() throws Exception {

        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("test-limit-runtime-public-image");
        request.setStartupScript("");
        request.setMaxRunDuration(Long.valueOf(120));

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
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.maxRunDuration").value(120));

        // Cleanup only need instance name
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
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

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setSubnetName("test-subnet-compute");
        request.setAddress(addressDTO);

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

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setSubnetName("test-subnet-compute");
        request.setAddress(addressDTO);

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
    @WithUserDetails(value = "computetutor@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getInstanceStatus_whenInstanceNameGiven() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("test-get-instance-status");
        request.setStartupScript("");

        SourceImageDTO sourceImageDTO = new SourceImageDTO();
        sourceImageDTO.setName("debian-11");
        sourceImageDTO.setProject("debian-cloud");
        request.setSourceImage(sourceImageDTO);

        MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
        machineTypeDTO.setName("e2-micro");
        request.setMachineType(machineTypeDTO);

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setSubnetName("test-subnet-compute");
        request.setAddress(addressDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        // Create an instance
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Get instance status
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Cleanup
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithUserDetails(value = "computetutor@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void resetInstance_whenInstanceNameGiven() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("test-reset-instance");
        request.setStartupScript("");

        SourceImageDTO sourceImageDTO = new SourceImageDTO();
        sourceImageDTO.setName("debian-11");
        sourceImageDTO.setProject("debian-cloud");
        request.setSourceImage(sourceImageDTO);

        MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
        machineTypeDTO.setName("e2-micro");
        request.setMachineType(machineTypeDTO);

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setSubnetName("test-subnet-compute");
        request.setAddress(addressDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        // Create an instance
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Reset instance
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Cleanup
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // @Test
    // void stopInstance_whenInstanceNameGiven() throws Exception {
    // ComputeDTO request = new ComputeDTO();
    // request.setInstanceName("test-stop-instance");
    // request.setStartupScript("");
    //
    // SourceImageDTO sourceImageDTO = new SourceImageDTO();
    // sourceImageDTO.setName("debian-11");
    // sourceImageDTO.setProject("debian-cloud");
    // request.setSourceImage(sourceImageDTO);
    //
    // MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
    // machineTypeDTO.setName("e2-micro");
    // request.setMachineType(machineTypeDTO);
    //
    // String jsonString = objectMapper.writeValueAsString(request);
    //
    // this.mockMvc
    // .perform(MockMvcRequestBuilders.post("/compute/create")
    // .contentType(MediaType.APPLICATION_JSON)
    // .content(jsonString))
    // .andExpect(MockMvcResultMatchers.status().isOk());
    //
    // // Stop instance
    // mockMvc
    // .perform(MockMvcRequestBuilders.post("/compute/stop")
    // .contentType(MediaType.APPLICATION_JSON)
    // .content(jsonString))
    // .andExpect(MockMvcResultMatchers.status().isOk())
    // .andReturn();
    // }

    @Test
    @WithUserDetails(value = "computetutor@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void stopThenStartAnInstance_whenInstanceNameGiven() throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName("test-stop-instance");
        request.setStartupScript("");

        SourceImageDTO sourceImageDTO = new SourceImageDTO();
        sourceImageDTO.setName("debian-11");
        sourceImageDTO.setProject("debian-cloud");
        request.setSourceImage(sourceImageDTO);

        MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
        machineTypeDTO.setName("e2-micro");
        request.setMachineType(machineTypeDTO);

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setSubnetName("test-subnet-compute");
        request.setAddress(addressDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        // Create an instance
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Stop instance
        mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/stop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Start instance
        mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Cleanup
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void failResetInstance_whenInvalidParametersGiven() throws Exception {

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Invalid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    @Test
    void failGetInstanceStatus_whenInvalidParametersGiven() throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Invalid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    @Test
    void failStopInstance_whenInvalidParametersGiven() throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/stop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Invalid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    @Test
    void failStartInstance_whenInvalidParametersGiven() throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Invalid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    @Test
    void addComputeInstanceUsers_whenValidParametersGiven() throws Exception {
        // Create an entry in database will do, as this doesn't need GCP to test
        Subnet subnet = new Subnet("test-subnet", "10.10.1.0/24");
        subnetRepository.save(subnet);
        Compute compute = new Compute("mock-entry", "e2-micro", "10.10.1.1", null, subnet);
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

        // Clean up manually as teardown() will fail since no actual instance is
        // created
        computeRepository.deleteByInstanceName(request.getInstanceName());
        subnetRepository.deleteBySubnetName("test-subnet");
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
        Subnet subnet = new Subnet("test-subnet", "10.10.1.0/24");
        subnetRepository.save(subnet);
        Compute compute = new Compute("mock-entry-remove", "e2-micro", "10.10.1.1",
                users, subnet);
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

        // Clean up manually as teardown() will fail since no actual instance is
        // created
        computeRepository.deleteByInstanceName(request.getInstanceName());
        subnetRepository.deleteBySubnetName("test-subnet");
        userRepository.deleteByEmail(userDTO.getEmail());
    }
}
