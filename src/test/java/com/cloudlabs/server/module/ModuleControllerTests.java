package com.cloudlabs.server.module;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudlabs.server.compute.Compute;
import com.cloudlabs.server.compute.ComputeRepository;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.module.dto.ModuleDTO;
import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.role.RoleRepository;
import com.cloudlabs.server.role.RoleType;
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
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
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

    @Autowired
    private RoleRepository roleRepository;

    @BeforeAll
    public void setup() throws Exception {
        Subnet subnet = subnetRepository.findBySubnetName("test-subnet-module");

        if (subnet == null) {
            subnetRepository.save(new Subnet("test-subnet-module", "10.254.4.0/24"));
        }

        // Create new test user
        User user = new User();
        user.setUsername("tester");
        user.setFullname("BobTester");
        user.setEmail("tester@gmail.com");
        user.setPassword("test@123");
        userRepository.save(user);
        Role tutorRole = roleRepository.findByName(RoleType.TUTOR);
        if (tutorRole == null) {
                tutorRole = new Role(RoleType.TUTOR);
        }
        Set<Role> roles = new HashSet<>(Arrays.asList(tutorRole));
        user.setRoles(roles);
        userRepository.save(user);
    }

    @AfterAll
    void teardown() throws Exception {
        subnetRepository.deleteBySubnetName("test-subnet-module");
        userRepository.deleteByEmail("tester@gmail.com");
    }

    @Test
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createModule_whenValidParametersGiven() throws Exception {
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

        // Clean up
        moduleRepository.deleteById(response.getModuleId());
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
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getAllModules() throws Exception {
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

        mockMvc.perform(MockMvcRequestBuilders.get("/Modules"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        ModuleDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ModuleDTO.class);

        // Clean up
        moduleRepository.deleteById(response.getModuleId());
    }

    @Test
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

        mockMvc
                .perform(MockMvcRequestBuilders.get("/Modules/{moduleId}",
                        response.getModuleId()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Clean up
        moduleRepository.deleteById(response.getModuleId());
    }

    @Test
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

        Long nonExistentModuleId = 999L; // Uses non-existent module id for test

        mockMvc
                .perform(MockMvcRequestBuilders.get("/Modules/{moduleId}",
                        nonExistentModuleId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        // Clean up
        moduleRepository.deleteById(response.getModuleId());
    }

    @Test
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

        mockMvc
                .perform(MockMvcRequestBuilders.delete("/Modules/delete/{moduleId}",
                        response.getModuleId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

        Long nonExistentModuleId = 999L; // uses non-existent module id

        mockMvc
                .perform(MockMvcRequestBuilders.delete("/Modules/delete/{moduleId}",
                        nonExistentModuleId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        // Clean up
        moduleRepository.deleteById(response.getModuleId());
    }

    @Test
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

        response.setModuleSubtitle("newsubtitle");
        response.setModuleName("");
        response.setModuleDescription("");

        String newjsonString = objectMapper.writeValueAsString(response);

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .put("/Modules/update/{moduleId}", response.getModuleId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(newjsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Clean up
        moduleRepository.deleteById(response.getModuleId());
    }

    @Test
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

        response.setModuleSubtitle("newsubtitle");
        response.setModuleName("");
        response.setModuleDescription("");

        String newjsonString = objectMapper.writeValueAsString(response);
        Long nonExistentModuleId = 999L; // uses non-existent module id

        mockMvc
                .perform(MockMvcRequestBuilders
                        .put("/Modules/update/{moduleId}", nonExistentModuleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newjsonString))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        // Clean up
        moduleRepository.deleteById(response.getModuleId());
    }

    @Test
    @WithUserDetails(value = "tester@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void addAndListUsersModules_whenValidParametersGiven() throws Exception {
        Module module = new Module("subtitle", "name", "description");
        moduleRepository.save(module);

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("tester@gmail.com");
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

        assertFalse(
                moduleRepository.findByUsers_Email(userDTO.getEmail()).isEmpty());

        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/Modules/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Clean up
        moduleRepository.deleteById(module.getModuleId());
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

        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/Modules/list-users-modules"))
                .andExpect(MockMvcResultMatchers.status().isOk());

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
        
        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/Modules/add-computes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

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
