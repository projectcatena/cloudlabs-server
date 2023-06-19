package com.cloudlabs.server.image;

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
public class ImageControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ImageService imageService;

    @Test
    void getSignedURL_whenFileTypeIsVMDK() throws Exception {
        ImageDTO image = new ImageDTO();
        image.setObjectName("virtual-disk.vmdk");

        String jsonString = objectMapper.writeValueAsString(image);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/image/signed")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("https://storage.googleapis.com/knobby_maple/" + image.getObjectName())));
    } 

    @Test
    void getSignedURL_whenFileTypeIsVHD() throws Exception {
        ImageDTO image = new ImageDTO();
        image.setObjectName("virtual-disk.vhd");

        String jsonString = objectMapper.writeValueAsString(image);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/image/signed")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("https://storage.googleapis.com/knobby_maple/" + image.getObjectName())));
    } 

    @Test
    void failGetSignedURL_whenFileTypeIsNotVMDKOrVHD() throws Exception {
        ImageDTO image = new ImageDTO();
        image.setObjectName("image.img");

        String jsonString = objectMapper.writeValueAsString(image);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/image/signed")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    } 

    @Test
    void failGetSignedURL_whenNoFileType() throws Exception {
        ImageDTO image = new ImageDTO();
        image.setObjectName("image");

        String jsonString = objectMapper.writeValueAsString(image);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/image/signed")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    } 

    @Test
    void failGetSignedURL_whenNoParametersGiven() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/image/signed")
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
        ImageDTO image = new ImageDTO();
        // Test image must already exist in GCP bucket
        image.setObjectName("Windows_Server_2019-disk1.vmdk");
        image.setImageName("windows-server-2019");

        String jsonString = objectMapper.writeValueAsString(image);
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/image/start")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.objectName").value(image.getObjectName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageName").value(image.getImageName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.buildId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.buildStatus").value("QUEUED"))
                .andReturn();
        
        // After launching a build, should cancel it to prevent unnecessary costly builds.
        ImageDTO responseFile = objectMapper.readValue(response.getResponse().getContentAsString(), ImageDTO.class);
        Build cancelBuildResponse = imageService.cancelVirtualDiskBUild(responseFile.getBuildId());

        assertNotNull(cancelBuildResponse);
    } 

    @Test
    void failStartBuild_whenParametersNotGiven() throws Exception {
        ImageDTO image = new ImageDTO();

        String jsonString = objectMapper.writeValueAsString(image);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/image/start")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void failStartBuild_whenFileDoesNotExistInBucket() throws Exception {
        // Generate random string
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));

        ImageDTO image = new ImageDTO();
        image.setObjectName(generatedString + ".vmdk");
        image.setImageName("test-image-does-not-exist");

        String jsonString = objectMapper.writeValueAsString(image);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/image/start")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
