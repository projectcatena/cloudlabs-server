package com.cloudlabs.server.file;

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
public class FileControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private FileService fileService;

    @Test
    void getSignedURL_whenFileTypeIsVMDK() throws Exception {
        FileDTO file = new FileDTO();
        file.setObjectName("file.vmdk");

        String jsonString = objectMapper.writeValueAsString(file);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/storage/signed")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("https://storage.googleapis.com/knobby_maple/" + file.getObjectName())));
    } 

    @Test
    void getSignedURL_whenFileTypeIsVHD() throws Exception {
        FileDTO file = new FileDTO();
        file.setObjectName("file.vhd");

        String jsonString = objectMapper.writeValueAsString(file);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/storage/signed")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("https://storage.googleapis.com/knobby_maple/" + file.getObjectName())));
    } 

    @Test
    void failGetSignedURL_whenFileTypeIsNotVMDKOrVHD() throws Exception {
        FileDTO file = new FileDTO();
        file.setObjectName("file.img");

        String jsonString = objectMapper.writeValueAsString(file);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/storage/signed")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    } 

    @Test
    void failGetSignedURL_whenNoFileType() throws Exception {
        FileDTO file = new FileDTO();
        file.setObjectName("file");

        String jsonString = objectMapper.writeValueAsString(file);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/storage/signed")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    } 

    @Test
    void failGetSignedURL_whenNoParametersGiven() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/storage/signed")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {
                        }
                        """))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    } 

    /**
     * Expensive test, as it will launch an actual build on GCP and return its build ID.
     * Solution: Cancel build using the returned build ID before completion.
     * 
     * @throws Exception
     */
    @Test
    void startBuild_whenCorrectParametersGiven() throws Exception {
        FileDTO file = new FileDTO();
        // Test file must already exist in GCP bucket
        file.setObjectName("Windows_Server_2019-disk1.vmdk");
        file.setImageName("windows-server-2019");

        String jsonString = objectMapper.writeValueAsString(file);
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/storage/start")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.objectName").value(file.getObjectName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageName").value(file.getImageName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.buildId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.buildStatus").value("QUEUED"))
                .andReturn();
        
        // After launching a build, should cancel it to prevent unnecessary costly builds.
        FileDTO responseFile = objectMapper.readValue(response.getResponse().getContentAsString(), FileDTO.class);
        Build cancelBuildResponse = fileService.cancelVirtualDiskBUild(responseFile.getBuildId());

        assertNotNull(cancelBuildResponse);
    } 

    @Test
    void failStartBuild_whenParametersNotGiven() throws Exception {
        FileDTO file = new FileDTO();

        String jsonString = objectMapper.writeValueAsString(file);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/storage/start")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void failStartBuild_whenFileDoesNotExistInBucket() throws Exception {
        // Generate random string
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));

        FileDTO file = new FileDTO();
        file.setObjectName(generatedString + ".vmdk");
        file.setImageName("test-file-does-not-exist");

        String jsonString = objectMapper.writeValueAsString(file);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/storage/start")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
