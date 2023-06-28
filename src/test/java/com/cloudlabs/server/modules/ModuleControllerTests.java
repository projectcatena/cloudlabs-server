package com.cloudlabs.server.modules;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.Charset;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloudbuild.v1.Build;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ModuleControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ModuleRepository repository;

    @Test
    void createModule_whenParametersGiven() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON).content("""
                {
                    \"subtitle\": \"test\",
                    \"title\": \"test\",
                    \"description\": \"for testing\"
                }
                """)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void failcreateModule_whenParametersnotGiven() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON).content("""
                
                """))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void getAllModules() throws Exception {
        // Create a module with known values
        Module module = new Module();
        module.setModuleId(1L);
        module.setModuleSubtitle("test");
        module.setModuleName("test");
        module.setModuleDescription("for testing");

        // Save the module to the repository
        this.repository.save(module);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/Modules"))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getModuleById_whenValidModuleIdGiven() throws Exception {
        // Create a module with known values
        Module module = new Module();
        module.setModuleId(1L);
        module.setModuleSubtitle("test");
        module.setModuleName("test");
        module.setModuleDescription("for testing");

        // Save the module to the repository
        this.repository.save(module);

        // Perform the GET request to retrieve the module by ID
        this.mockMvc.perform(MockMvcRequestBuilders.get("/Modules/{moduleId}", module.getModuleId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getModuleById_whenInvalidModuleIdGiven() throws Exception {
        String invalidModuleId = "abc"; // Invalid module ID

        this.mockMvc.perform(MockMvcRequestBuilders.get("/Modules/{moduleId}", invalidModuleId))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void getModuleById_whenModuleNotFound() throws Exception {
        Long nonExistentModuleId = 999L; // Non-existent module ID

        // Perform the GET request with an Non-existent module ID
        this.mockMvc.perform(MockMvcRequestBuilders.get("/Modules/{moduleId}", nonExistentModuleId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deleteModule_whenValidModuleIdGiven() throws Exception {
        // Create a module with known values
        Module module = new Module();
        module.setModuleId(1L);
        module.setModuleSubtitle("test");
        module.setModuleName("test");
        module.setModuleDescription("for testing");

        // Save the module to the repository
        this.repository.save(module);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/Modules/delete/{moduleId}", module.getModuleId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void deleteModule_whenInvalidModuleIdFormatGiven() throws Exception {
        String invalidModuleId = "abc"; // Invalid module ID format

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/Modules/delete/{moduleId}", invalidModuleId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void deleteModule_whenModuleNotFound() throws Exception {
        Long nonExistentModuleId = 999L; // Non-existent module ID

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/Modules/delete/{moduleId}", nonExistentModuleId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void updateModule_whenValidModuleIdAndValidDataGiven() throws Exception {
        // Create a module with known values
        Module module = new Module();
        module.setModuleId(1L);
        module.setModuleSubtitle("old subtitle");
        module.setModuleName("old title");
        module.setModuleDescription("old description");

        // Save the module to the repository
        this.repository.save(module);

        // Prepare the request body with updated data
        this.mockMvc.perform(MockMvcRequestBuilders.put("/Modules/update/{moduleId}", module.getModuleId())
        .contentType(MediaType.APPLICATION_JSON).content("""
                {
                    \"subtitle\": \"new subtitle\",
                    \"title\": \"new title\",
                    \"description\": \"new description\"
                }
                """)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void updateModule_whenValidIdandPartialDataGiven() throws Exception {
        // Create a module with known values
        Module module = new Module();
        module.setModuleId(1L);
        module.setModuleSubtitle("old subtitle");
        module.setModuleName("old title");
        module.setModuleDescription("old description");

        // Save the module to the repository
        this.repository.save(module);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/Modules/update/{moduleId}", module.getModuleId())
        .contentType(MediaType.APPLICATION_JSON).content("""
                {
                    \"subtitle\": \"new subtitle\"
                    \"title\": \"old title\",
                    \"description\": \"old description\"
                }
                """)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void updateModule_whenInvalidModuleIdGiven() throws Exception {
        String invalidModuleId = "abc"; // Invalid module ID format

        this.mockMvc.perform(MockMvcRequestBuilders.put("/Modules/update/{moduleId}", invalidModuleId)
        .contentType(MediaType.APPLICATION_JSON).content("""
                {
                    \"subtitle\": \"test\",
                    \"title\": \"test\",
                    \"description\": \"for testing\"
                }
                """)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void updateModule_whenModuleNotFound() throws Exception {
        Long nonExistentModuleId = 999L; // Non-existent module ID

        this.mockMvc.perform(MockMvcRequestBuilders.put("/Modules/update/{moduleId}", nonExistentModuleId)
        .contentType(MediaType.APPLICATION_JSON).content("""
                {
                    \"subtitle\": \"test\",
                    \"title\": \"test\",
                    \"description\": \"for testing\"
                }
                """)).andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
