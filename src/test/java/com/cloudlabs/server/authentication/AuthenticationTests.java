package com.cloudlabs.server.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthenticationTests {

    @Autowired
    protected MockMvc mockMvc;

    private static String username1 = "user1";

    private static String uid2 = "user2";

    private static String username3 = "user3";

    private static String pw1 = "1234";

    private static String token = null;

    // @RegisterExtension
    // static WireMockExtension wireMockServer = WireMockExtension.newInstance()
    //     .options(WireMockConfiguration.wireMockConfig().dynamicPort())
    //     .build();

    // @DynamicPropertySource
    // static void configureProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
    //     dynamicPropertyRegistry.add("gcp_base_url", wireMockServer::baseUrl);
    // }

    /**
     * Simulates a user input collected from front-end to create and launch a compute instance.
     * MockServer will response with success, when instance is created and launched.
     *
     * @throws Exception
     */

    /*Retrieving Jwt Token
    @Test
    void GetJwtToken_whenCredentialsGiven() throws Exception {
        // wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/compute/create"))
        //     .willReturn(WireMock.aResponse()
        //     .withStatus(200)
        //     .withHeader("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
        //     .withBody("""
        //     {
        //         \"status\": \"success\",
        //     }
        //     """)));

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("username", username1)
            .param("password", pw1))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
    }
    */
    
    @Test
    void GetJwtToken_whenCredentialsNotGiven() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("username", "")
            .param("password", ""))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void GetJwtToken_whenWrongCredentialsGiven() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .param("username",username1)
        .param("password","1111"))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    //Retrieving and Authenticating with Jwt Token
    @Test
    void JwtAuthentication_whenCorrectTokenGiven() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("username", username1)
            .param("password", pw1))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        Integer size = token.length();
        token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/module")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void PageAccess_withNoPriviledge() throws Exception{
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("username", username1)
            .param("password", pw1))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        Integer size = token.length();
        token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/tutor")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
    
    @Test
    void PageAccess_withPriviledge() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("username", username3)
            .param("password", pw1))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        Integer size = token.length();
        token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/tutor")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }
    
}
