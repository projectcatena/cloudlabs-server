package com.cloudlabs.server.module;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudlabs.server.compute.Compute;
import com.cloudlabs.server.compute.ComputeRepository;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.module.dto.ModuleDTO;
import com.cloudlabs.server.subnet.Subnet;
import com.cloudlabs.server.subnet.SubnetRepository;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.dto.UserDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@WithMockUser(username = "tutor", roles = { "TUTOR" })
public class ModuleControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private SubnetRepository subnetRepository;

    @BeforeAll
    public void setup() throws Exception {
        Subnet subnet = subnetRepository.findBySubnetName("test-subnet-module");

        if (subnet == null) {
            // Pre-configured on GCP
            subnetRepository.save(new Subnet("test-subnet-module", "10.254.4.0/24"));
        }
    }

    @AfterAll
    void teardown() throws Exception {
        subnetRepository.deleteBySubnetName("test-subnet-module");
    }

    @Test
    void createModule_whenValidParametersGiven() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @WithMockUser(username = "user", roles = { "USER" })
    @Test
    void failCreateModule_whenInvalidParametersGivenAndNormalUserRole()
            throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("");
        request.setModuleName("");
        request.setModuleDescription("");

        String jsonString = objectMapper.writeValueAsString(request);

        mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/create")
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

        mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
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

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);

        mockMvc
                .perform(MockMvcRequestBuilders.get("/Modules/{moduleId}",
                        savedResponse.getModuleId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getModuleById_whenModuleNotFound() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);
        Long nonExistentModuleId = 999L; // Uses non-existent module id for test

        mockMvc
                .perform(MockMvcRequestBuilders.get("/Modules/{moduleId}",
                        nonExistentModuleId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deleteModuleById_whenValidParametersGiven() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);

        mockMvc
                .perform(MockMvcRequestBuilders.delete("/Modules/delete/{moduleId}",
                        savedResponse.getModuleId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void deleteModuleById_WhenModuleNotFound() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);
        Long nonExistentModuleId = 999L; // uses non-existent module id

        mockMvc
                .perform(MockMvcRequestBuilders.delete("/Modules/delete/{moduleId}",
                        nonExistentModuleId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void updateModule_whenValidModuleIdAndPartialDataGiven() throws Exception {
        ModuleDTO request = new ModuleDTO();
        request.setModuleSubtitle("Subtitle");
        request.setModuleName("Name");
        request.setModuleDescription("Description");

        String jsonString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);

        savedResponse.setModuleSubtitle("newsubtitle");
        savedResponse.setModuleName("");
        savedResponse.setModuleDescription("");

        String newjsonString = objectMapper.writeValueAsString(savedResponse);

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .put("/Modules/update/{moduleId}", savedResponse.getModuleId())
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

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        ModuleDTO savedResponse = moduleService.addModule(response);

        savedResponse.setModuleSubtitle("newsubtitle");
        savedResponse.setModuleName("");
        savedResponse.setModuleDescription("");

        String newjsonString = objectMapper.writeValueAsString(savedResponse);
        Long nonExistentModuleId = 999L; // uses non-existent module id

        mockMvc
                .perform(MockMvcRequestBuilders
                        .put("/Modules/update/{moduleId}", nonExistentModuleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newjsonString))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    @Test
    void addAndListModuleUsers_whenValidParametersGiven() throws Exception {
        Module module = new Module("subtitle", "name", "description");
        moduleRepository.save(module);

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

        ModuleDTO request = new ModuleDTO();
        request.setModuleName("name");
        request.setModuleId(module.getModuleId());
        request.setUsers(userDTOs);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/add-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        assertFalse(
                moduleRepository.findByUsers_Email(userDTO.getEmail()).isEmpty());

        MvcResult result = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/list-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        List<UserDTO> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<UserDTO>>() {
                });

        assertFalse(response.isEmpty());

        // Clean up
        moduleRepository.deleteById(module.getModuleId());
        userRepository.deleteByEmail(userDTO.getEmail());
    }

    @Test
    void removeModuleUsers_whenValidParametersGiven() throws Exception {

        User user = new User();
        user.setUsername("test4");
        user.setFullname("John");
        user.setEmail("test4@gmail.com");
        user.setPassword("test@123");

        Set<User> users = new HashSet<>();
        users.add(user);

        // Create an entry in database will do, as this doesn't need GCP to test
        Module module = new Module("subtitle", "mock-remove", "description", users);
        moduleRepository.save(module);

        assertNotNull(moduleRepository.findByUsers_Email(user.getEmail()));

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test4@gmail.com");
        List<UserDTO> userDTOs = Arrays.asList(userDTO);

        ModuleDTO request = new ModuleDTO();
        request.setModuleName("name");
        request.setModuleId(module.getModuleId());
        request.setUsers(userDTOs);

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/remove-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        assertTrue(
                moduleRepository.findByUsers_Email(userDTO.getEmail()).isEmpty());

        // Clean up
        moduleRepository.deleteById(module.getModuleId());
        userRepository.deleteByEmail(userDTO.getEmail());
    }

    @Test
    void addComputeInstance_whenValidParametersGiven() throws Exception {
        Module module = new Module("subtitle", "name", "description");
        moduleRepository.save(module);

        Compute compute = new Compute();
        compute.setInstanceName("instance-test-linkmod");
        compute.setPrivateIPv4Address("10.10.10.1");
        compute.setMachineType("e2-medium");
        compute.setSubnet(subnetRepository.findBySubnetName("test-subnet-module"));
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
        compute.setPrivateIPv4Address("10.10.10.1");
        compute.setMachineType("e2-medium");
        compute.setSubnet(subnetRepository.findBySubnetName("test-subnet-module"));
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
        compute.setPrivateIPv4Address("10.10.10.2");
        compute.setMachineType("e2-medium");
        compute.setSubnet(subnetRepository.findBySubnetName("test-subnet-module"));
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
        compute.setPrivateIPv4Address("10.10.10.2");
        compute.setMachineType("e2-medium");
        compute.setSubnet(subnetRepository.findBySubnetName("test-subnet-module"));
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
