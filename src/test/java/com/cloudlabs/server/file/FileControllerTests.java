package com.cloudlabs.server.file;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class FileControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
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
        this.mockMvc.perform(MockMvcRequestBuilders.post("/storage/start")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.objectName").value(file.getObjectName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageName").value(file.getImageName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.buildStatus").value("QUEUED"));
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
