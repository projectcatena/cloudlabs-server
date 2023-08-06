package com.cloudlabs.server.snapshot;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.cloudlabs.server.compute.dto.AddressDTO;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import com.cloudlabs.server.compute.dto.SourceImageDTO;
import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleType;
import com.cloudlabs.server.snapshot.dto.SaveSnapshotDTO;
import com.cloudlabs.server.subnet.SubnetRepository;
import com.cloudlabs.server.subnet.SubnetService;
import com.cloudlabs.server.subnet.dto.SubnetDTO;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@WithMockUser(username = "tutor", roles = { "TUTOR" })
public class SnapshotControllerTests {
    
    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubnetService subnetService;

    @Autowired
    private SubnetRepository subnetRepository;

    ComputeDTO createInstance(String instanceName) throws Exception {
        ComputeDTO request = new ComputeDTO();
        request.setInstanceName(instanceName);
        request.setStartupScript("");

        SourceImageDTO sourceImageDTO = new SourceImageDTO();
        sourceImageDTO.setName("windows-server-2019");
        request.setSourceImage(sourceImageDTO);

        MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
        machineTypeDTO.setName("e2-medium");
        request.setMachineType(machineTypeDTO);

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setSubnetName("test-subnet-snapshot");
        request.setAddress(addressDTO);

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/compute/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        
        ComputeDTO response = objectMapper.readValue(
            result.getResponse().getContentAsString(), ComputeDTO.class);
        
        return response;
    }

    void deleteAfterUse(String jsonString, ComputeDTO response) throws Exception {
         // Delete instance and release its public IP Address after test
        this.mockMvc.perform((MockMvcRequestBuilders.post("/compute/delete"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(response)))
        .andExpect(MockMvcResultMatchers.status().isOk());

        //Delete snapshot after testing
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/snapshot/delete")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString))
        .andExpect(MockMvcResultMatchers.status().isOk());

        //userRepository.deleteByEmail("computetutor@gmail.com");
        
    }

    @BeforeAll
    void subnet_setup() {
        SubnetDTO request = new SubnetDTO();
        request.setSubnetName("test-subnet-snapshot");
        request.setIpv4Range("10.254.3.0/24");
        subnetService.createSubnet(request);
    }

    @BeforeEach
    void setup(@Autowired JdbcTemplate jdbcTemplate) throws Exception {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_roles");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "user_table");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "roles");
        User user = new User("Bobby", "tutor", "test@gmail.com", "Pa$$w0rd",
                new HashSet<>(Arrays.asList(new Role(RoleType.TUTOR))));
        userRepository.save(user);

    }

    @AfterAll
    void teardown() throws Exception {
        userRepository.deleteByEmail("test@gmail.com");
        subnetService.deleteSubnet("test-subnet-snapshot");
    }
    
    @Test
    @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createSnapshot() throws Exception {
        ComputeDTO response = createInstance("test-instance-for-create-snapshot-success");

        SaveSnapshotDTO saveSnapshotDTO = new SaveSnapshotDTO("snapshot-1-success",
        "", response.getInstanceName());

        String jsonString = objectMapper.writeValueAsString(saveSnapshotDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/snapshot/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        deleteAfterUse(jsonString, response);
    }

    @Test
    @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteSnapshot() throws Exception {
        // create the instance
        ComputeDTO response = createInstance("test-instance-for-delete-snapshot-success");

        SaveSnapshotDTO saveSnapshotDTO = new SaveSnapshotDTO("snapshot-2-success",
        "", response.getInstanceName());

        String jsonString = objectMapper.writeValueAsString(saveSnapshotDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/snapshot/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isOk());

        // delete request
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/snapshot/delete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
         // Delete instance and release its public IP Address after test
        this.mockMvc.perform((MockMvcRequestBuilders.post("/compute/delete"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(response)))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void listSnapshots() throws Exception {
        // create the instance
        ComputeDTO response = createInstance("test-instance-for-list-snapshot");

        SaveSnapshotDTO saveSnapshotDTO = new SaveSnapshotDTO("snapshot-0",
        "", response.getInstanceName());

        String jsonString = objectMapper.writeValueAsString(saveSnapshotDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/snapshot/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        this.mockMvc.perform(MockMvcRequestBuilders.get("/snapshot/list"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        deleteAfterUse(jsonString, response);
    }

    @Test
    @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void revert_whenSnapshotExists() throws Exception {
        // create the instance
        ComputeDTO response = createInstance("test-instance-for-revert-snapshot-success");

        SaveSnapshotDTO saveSnapshotDTO = new SaveSnapshotDTO("snapshot-3-success",
        "", response.getInstanceName());

        String jsonString = objectMapper.writeValueAsString(saveSnapshotDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/snapshot/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isOk());

        // revert to snapshot
        this.mockMvc.perform(MockMvcRequestBuilders.post("/snapshot/revert")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        deleteAfterUse(jsonString, response);
    }

    @Test
    @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void revert_whenSnapshotDoesNotExist() throws Exception {
        // create the instance
        ComputeDTO response = createInstance("test-instance-for-revert-snapshot-failure");

        SaveSnapshotDTO saveSnapshotDTO = new SaveSnapshotDTO("snapshot-3-failure",
        "", response.getInstanceName());

        String jsonString = objectMapper.writeValueAsString(saveSnapshotDTO);

        // revert to snapshot
        this.mockMvc.perform(MockMvcRequestBuilders.post("/snapshot/revert")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
    }
}