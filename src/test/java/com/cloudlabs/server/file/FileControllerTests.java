package com.cloudlabs.server.file;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
// @ExtendWith(MockitoExtension.class)
public class FileControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    // @Autowired
    // protected FileService fileService;

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
}
