package com.cloudlabs.server.snapshot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.cloudlabs.server.compute.ComputeService;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import com.cloudlabs.server.compute.dto.SourceImageDTO;
import com.cloudlabs.server.snapshot.dto.SaveSnapshotDTO;
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
    private SnapshotService snapshotService;

    @Autowired
    private ComputeService computeService;

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

    
    @Test
    void createSnapshot() throws Exception {
        ComputeDTO response = createInstance("test-instance-for-create-snapshot-success");

        SaveSnapshotDTO saveSnapshotDTO = new SaveSnapshotDTO("snapshot-1-success",
        "", response.getInstanceName());

        String jsonString = objectMapper.writeValueAsString(saveSnapshotDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/snapshot/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
         // Delete instance and release its public IP Address after test
        computeService.releaseStaticExternalIPAddress(
                response.getAddress().getName());
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(response.getInstanceName());
        assertNotNull(deleteComputeDTO.getStatus());
        //Delete snapshot after testing
        snapshotService.deleteSnapshot(saveSnapshotDTO.getSnapshotName());
    }

    @Test
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
        computeService.releaseStaticExternalIPAddress(
                response.getAddress().getName());
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(response.getInstanceName());
        assertNotNull(deleteComputeDTO.getStatus());
    }

    @Test
    void listSnapshots() throws Exception {
        // create the instance
        ComputeDTO response = createInstance("test-instance-for-list-snapshot");

        SaveSnapshotDTO saveSnapshotDTO = new SaveSnapshotDTO("snapshot-0",
        "", response.getInstanceName());

        String jsonString = objectMapper.writeValueAsString(saveSnapshotDTO);

        // delete request
        this.mockMvc.perform(MockMvcRequestBuilders.post("/snapshot/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        this.mockMvc.perform(MockMvcRequestBuilders.get("/snapshot/list"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        // Delete instance and release its public IP Address after test
        computeService.releaseStaticExternalIPAddress(
                response.getAddress().getName());
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(response.getInstanceName());
        assertNotNull(deleteComputeDTO.getStatus());
        // delete snapshot
        snapshotService.deleteSnapshot(saveSnapshotDTO.getSnapshotName());
    }

    @Test
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
        
        // Delete instance and release its public IP Address after test
        computeService.releaseStaticExternalIPAddress(
                response.getAddress().getName());
        ComputeDTO deleteComputeDTO = computeService.deleteInstance(response.getInstanceName());
        assertNotNull(deleteComputeDTO.getStatus());
        // delete snapshot
        snapshotService.deleteSnapshot(saveSnapshotDTO.getSnapshotName());
    }

    @Test
    void revert_whenSnapshotDoesNotExist() throws Exception {
        // create the instance
        ComputeDTO response = createInstance("test-instance-for-revert-snapshot");

        SaveSnapshotDTO saveSnapshotDTO = new SaveSnapshotDTO("snapshot-3-failure",
        "", response.getInstanceName());

        String jsonString = objectMapper.writeValueAsString(saveSnapshotDTO);

        // revert to snapshot
        this.mockMvc.perform(MockMvcRequestBuilders.post("/snapshot/revert")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        computeService.releaseStaticExternalIPAddress(
                response.getAddress().getName());
    }
}
