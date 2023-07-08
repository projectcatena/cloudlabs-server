package com.cloudlabs.server.authentication;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
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
@TestInstance(Lifecycle.PER_CLASS)
public class AuthenticationTests {

    @Autowired
    protected MockMvc mockMvc;

    private static String name = "tester";

    private static String email = "tester@gmail.com";

    private static String pw = "password";

    private static String USER_name = "user";

    private static String USER_email = "user@gmail.com";

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
    /*
    @Test
    void SignUp_WithCorrrectInput_then_SignUp_withCreatedAccounts() throws Exception {
                this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("name", name)
                .param("email", email)
                .param("password", pw))
                .andExpect(MockMvcResultMatchers.status().isOk());

                this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("name", USER_name)
                .param("email", USER_email)
                .param("password", pw))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

                        this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("name", name)
                .param("email", email)
                .param("password", pw))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
}
*/

/*
@Test
void SignUp_WithIncorrectInput() throws Exception {
	this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    .param("name", "")
    .param("email", "")
    .param("password",""))
    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
}
*/

    @BeforeAll
    void SignUpw_withValidCredentials() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .param("name", name)
        .param("email", email)
        .param("password",pw))
        .andExpect(MockMvcResultMatchers.status().isOk());

        this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", USER_name)
            .param("email", USER_email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
    }

    @Test
    void SignUp_withAlreadyCreatedUser() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", name)
            .param("email", email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

    @Test
    void GetJwtToken_whenWrongCredentialsGiven() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .param("email", email)
        .param("password","1111"))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .param("email", USER_email)
        .param("password","1111"))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

     //Retrieving and Authenticating with Jwt Token
    @Test
    void JwtAuthentication_whenCorrectTokenGiven() throws Exception {

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/module")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void DeleteRole_withAdminRole_then_withoutAdminRole() throws Exception {

        MvcResult login_result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = login_result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/admin/delete")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header("Authorization", "Bearer " + token)
            .param("email", USER_email)
            .param("role", "ADMIN"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        SignOut();

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", USER_email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

	    this.mockMvc.perform(MockMvcRequestBuilders.delete("/admin/delete")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	        .header("Authorization", "Bearer " + token)
            .param("email", email)
            .param("role", "ADMIN"))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andReturn();
        SignOut();
}

@Test
    void AdminPageAccess_withNoPriviledge() throws Exception{
	MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", USER_email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

	    token = result.getResponse().getContentAsString();
        System.out.println("token" + token);
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/admin")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void AdminPageAccess_withPriviledge() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/admin")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isOk());
        SignOut();
}

    @Test
    void AdminPageAccess_withoutPriviledge() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", USER_email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/admin")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void ListUsers_withPriviledge() throws Exception {
            MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("email", email)
                .param("password", pw))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

            token = result.getResponse().getContentAsString();
            //Integer size = token.length();
            //token = token.substring(8,size-2);

            this.mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .header("Authorization", "Bearer " + token))
            .andExpect(MockMvcResultMatchers.status().isOk());
            SignOut();
    }

    /*
    @Test
        void ListUsers_withoutPriviledge() throws Exception {
            MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("email", USER_email)
                .param("password", pw))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

            token = result.getResponse().getContentAsString();
            System.out.println(token);
            //Integer size = token.length();
            //token = token.substring(8,size-2);

            this.mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .header("Authorization", "Bearer " + token))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
            SignOut();
    }
    */

    /*
    @Test
    void AddAdminRole_withoutCredentials_then_withCredentials() throws Exception{
	
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", USER_email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/admin/add")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .param("email", USER_email)
        .param("role", "ADMIN"))
        .andExpect(MockMvcResultMatchers.status().isForbidden());

        SignOut();

        MvcResult login_result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

	    token = login_result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/admin/add")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .param("email", USER_email)
        .param("role", "ADMIN"))
        .andExpect(MockMvcResultMatchers.status().isOk());
        SignOut();
    }
    */

    @Test
    void ModulePageAccess_withToken() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", USER_email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/module")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isOk());
        SignOut();
    }

    @Test
    void ModulePageAccess_withoutToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/module"))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    /*
    @Test
    void UsersPageAccess_withPriviledge() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", USER_email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/users")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isOk());
        SignOut();
    }
    */
    /*
    @Test
    void UsersPageAccess_withoutPriviledge() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        //Delete Tutor role from user
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/admin/delete")
            .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .param("email", USER_email)
        .param("role", "TUTOR"))
        .andExpect(MockMvcResultMatchers.status().isOk());

        SignOut();

        MvcResult user_result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", USER_email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        String new_token = user_result.getResponse().getContentAsString();
        //Integer size_ = token.length();
        //token = token.substring(8,size_-2);
    
        this.mockMvc.perform(MockMvcRequestBuilders.get("/users")
        .header("Authorization", "Bearer " + new_token))
        .andExpect(MockMvcResultMatchers.status().isForbidden());
        SignOut();
    }
    */

    @Test
    void AccountPageAccess_withToken() throws Exception {
    MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/account")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isOk());
        SignOut();
    }

    @Test
    void AccountPageAccess_withoutToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/account"))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void SignOut() throws Exception {
    MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("email", email)
            .param("password", pw))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        token = result.getResponse().getContentAsString();
        //Integer size = token.length();
        //token = token.substring(8,size-2);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/signout"))
        .andExpect(MockMvcResultMatchers.status().isOk());

        /*
        this.mockMvc.perform(MockMvcRequestBuilders.get("/account")
        .header("Authorization", "Bearer " + token))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        */
    }
}
