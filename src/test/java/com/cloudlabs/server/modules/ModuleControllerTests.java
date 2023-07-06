package com.cloudlabs.server.modules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.modules.dto.ModuleDTO;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ModuleControllerTest {
    
    @Autowired
    protected MockMvc mockMvc;

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

    @Test void failCreateModule_whenInvalidParametersGiven() throws Exception {
        ModuleDTO request = new ModuleDTO();    
        request.setModuleSubtitle("");
        request.setModuleName("");
        request.setModuleDescription("");

        String jsonString = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/Modules/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
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
}
