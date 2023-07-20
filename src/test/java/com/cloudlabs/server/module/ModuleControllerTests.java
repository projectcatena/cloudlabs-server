package com.cloudlabs.server.module;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.cloudlabs.server.compute.Compute;
import com.cloudlabs.server.compute.ComputeRepository;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.module.dto.ModuleDTO;
import com.cloudlabs.server.user.UserRepository;

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

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(username = "tutor", roles = { "TUTOR" })
public class ModuleControllerTests {
    
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ComputeRepository computeRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ModuleService moduleService;

    @Test
    void createModule_whenValidParametersGiven() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();


    }

    @WithMockUser(username = "user", roles = { "USER" })
    @Test 
    void failCreateModule_whenInvalidParametersGivenAndNormalUserRole() throws Exception {
        ModuleDTO request = new ModuleDTO();    
        request.setModuleSubtitle("");
        request.setModuleName("");
        request.setModuleDescription("");

        String jsonString = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString))
        .andExpect(MockMvcResultMatchers.status().isForbidden())
        .andReturn();
    }

    @Test
    void getAllModules() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();   

        moduleService.addModule(request);

        mockMvc.perform(MockMvcRequestBuilders.get("/Modules"))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getModuleById_whenValidParametersGiven() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/Modules/{moduleId}", savedResponse.getModuleId()))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getModuleById_whenModuleNotFound() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();  

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);
        Long nonExistentModuleId = 999L; //Uses non-existent module id for test

        mockMvc.perform(MockMvcRequestBuilders.get("/Modules/{moduleId}", nonExistentModuleId))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deleteModuleById_whenValidParametersGiven() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();  

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);

        mockMvc.perform(MockMvcRequestBuilders.delete("/Modules/delete/{moduleId}", savedResponse.getModuleId()))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void deleteModuleById_WhenModuleNotFound() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);
        Long nonExistentModuleId = 999L; //uses non-existent module id

        mockMvc.perform(MockMvcRequestBuilders.delete("/Modules/delete/{moduleId}", nonExistentModuleId))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void updateModule_whenValidModuleIdAndPartialDataGiven() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();  

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);

        savedResponse.setModuleSubtitle("newsubtitle");
        savedResponse.setModuleName("");
        savedResponse.setModuleDescription("");

        String newjsonString = objectMapper.writeValueAsString(savedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/Modules/update/{moduleId}", savedResponse.getModuleId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(newjsonString))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    }

    @Test
    void updateModule_whenInvalidModuleIdAndPartialDataGiven() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();  

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);

        savedResponse.setModuleSubtitle("newsubtitle");
        savedResponse.setModuleName("");
        savedResponse.setModuleDescription("");

        String newjsonString = objectMapper.writeValueAsString(savedResponse);
        Long nonExistentModuleId = 999L; //uses non-existent module id

        mockMvc.perform(MockMvcRequestBuilders.put("/Modules/update/{moduleId}", nonExistentModuleId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(newjsonString))
        .andExpect(MockMvcResultMatchers.status().isNotFound())
        .andReturn();
    }

    @Test
    void addComputeInstance_whenValidParametersGiven() throws Exception {
        Module module = new Module("subtitle", "name", "description");
        moduleRepository.save(module);

        Compute compute = new Compute();
        compute.setInstanceName("instance-test-linkmod");
        compute.setIpv4Address("10.10.10.1");
        compute.setMachineType("e2-medium");
        computeRepository.save(compute);

        ComputeDTO computeDTO = new ComputeDTO();
        computeDTO.setInstanceName("instance-test-linkmod");
        List<ComputeDTO> computeDTOs = Arrays.asList(computeDTO);

        ModuleDTO request = new ModuleDTO();
        request.setModuleId(module.getModuleId());
        request.setComputes(computeDTOs);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/add-computes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        moduleRepository.deleteById(module.getModuleId());
        computeRepository.deleteByInstanceName("instance-test-linkmod");
    }

    @Test
    void failaddComputeInstance_whenInstanceNotFound() throws Exception {
        Module module = new Module("subtitle", "name", "description");
        moduleRepository.save(module);

        Compute compute = new Compute();
        compute.setInstanceName("instance-test-linkmod");
        compute.setIpv4Address("10.10.10.1");
        compute.setMachineType("e2-medium");
        computeRepository.save(compute);

        ComputeDTO computeDTO = new ComputeDTO();
        computeDTO.setInstanceName("instance-test-notfound");
        List<ComputeDTO> computeDTOs = Arrays.asList(computeDTO);

        ModuleDTO request = new ModuleDTO();
        request.setModuleId(module.getModuleId());
        request.setComputes(computeDTOs);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/add-computes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        moduleRepository.deleteById(module.getModuleId());
        computeRepository.deleteByInstanceName("instance-test-linkmod");
    }

    @Test
    void removeComputeInstance_whenValidParametersGiven() throws Exception {
        Module module = new Module("subtitle", "name", "description");
        moduleRepository.save(module);

        Compute compute = new Compute();
        compute.setInstanceName("instance-test-removelinkmod");
        compute.setIpv4Address("10.10.10.2");
        compute.setMachineType("e2-medium");
        computeRepository.save(compute);

        ComputeDTO computeDTO = new ComputeDTO();
        computeDTO.setInstanceName("instance-test-removelinkmod");
        List<ComputeDTO> computeDTOs = Arrays.asList(computeDTO);

        ModuleDTO request = new ModuleDTO();
        request.setModuleId(module.getModuleId());
        request.setComputes(computeDTOs);

        // Link instance with module
        moduleService.addModuleComputeInstance(request);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/remove-computes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        moduleRepository.deleteById(module.getModuleId());
        computeRepository.deleteByInstanceName("instance-test-removelinkmod");
    }

    @Test
    void failremoveComputeInstance_whenInstanceNotFound() throws Exception {
        Module module = new Module("subtitle", "name", "description");
        moduleRepository.save(module);

        Compute compute = new Compute();
        compute.setInstanceName("instance-test-failremovelinkmod");
        compute.setIpv4Address("10.10.10.2");
        compute.setMachineType("e2-medium");
        computeRepository.save(compute);

        ComputeDTO computeDTO = new ComputeDTO();
        computeDTO.setInstanceName("instance-test-removelinkmod");
        List<ComputeDTO> computeDTOs = Arrays.asList(computeDTO);

        ModuleDTO request = new ModuleDTO();
        request.setModuleId(module.getModuleId());
        request.setComputes(computeDTOs);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/remove-computes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        moduleRepository.deleteById(module.getModuleId());
        computeRepository.deleteByInstanceName("instance-test-failremovelinkmod");
    }
}
