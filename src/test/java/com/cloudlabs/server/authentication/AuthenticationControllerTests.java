package com.cloudlabs.server.authentication;

import com.cloudlabs.server.security.auth.dto.LoginDTO;
import com.cloudlabs.server.security.auth.dto.RegisterDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
public class AuthenticationControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    void register_withValidParams() throws Exception {
        RegisterDTO request = new RegisterDTO("John Pork", "johnpork123",
                "john_pork@testmain.com", "Pa$$w0rd");

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void failRegister_whenUsernameTaken() throws Exception {

        RegisterDTO request = new RegisterDTO(
                "John Pork", "johnpork123", "john_pork2@testmain.com", "Pa$$w0rd");

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void failRegister_whenEmailTaken() throws Exception {

        RegisterDTO request = new RegisterDTO("John Pork", "johnpork123new",
                "john_pork@testmain.com", "Pa$$w0rd");

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void login_whenCredentialsGiven() throws Exception {
        LoginDTO request = new LoginDTO("john_pork@testmain.com", "Pa$$w0rd");

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void failLogin_whenWrongCredentialsGiven() throws Exception {
        LoginDTO request = new LoginDTO("john_pork@testmain.com", "wrong@password");

        String jsonString = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    // Retrieving and Authenticating with Jwt Token
    /*
     * @Test
     * void JwtAuthentication_whenCorrectTokenGiven() throws Exception {
     *
     * MvcResult result = this.mockMvc
     * .perform(
     * MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * // Integer size = token.length();
     * // token = token.substring(8,size-2);
     *
     * this.mockMvc
     * .perform(MockMvcRequestBuilders.get("/module").header(
     * "Authorization", "Bearer " + token))
     * .andExpect(MockMvcResultMatchers.status().isOk());
     * }
     *
     * @Test
     * void DeleteRole_withAdminRole_then_withoutAdminRole() throws Exception {
     *
     * MvcResult login_result = this.mockMvc
     * .perform(
     * MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = login_result.getResponse().getContentAsString();
     * // Integer size = token.length();
     * // token = token.substring(8,size-2);
     *
     * this.mockMvc
     * .perform(MockMvcRequestBuilders.delete("/admin/delete")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .header("Authorization", "Bearer " + token)
     * .param("email", USER_email)
     * .param("role", "ADMIN"))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * SignOut();
     *
     * MvcResult result = this.mockMvc
     * .perform(
     * MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * // Integer size = token.length();
     * // token = token.substring(8,size-2);
     *
     * this.mockMvc
     * .perform(MockMvcRequestBuilders.delete("/admin/delete")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .header("Authorization", "Bearer " + token)
     * .param("email", email)
     * .param("role", "ADMIN"))
     * .andExpect(MockMvcResultMatchers.status().isForbidden())
     * .andReturn();
     * SignOut();
     * }
     *
     * @Test
     * void AdminPageAccess_withNoPriviledge() throws Exception {
     * MvcResult result = this.mockMvc
     * .perform(
     * MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * System.out.println("token" + token);
     * // Integer size = token.length();
     * // token = token.substring(8,size-2);
     *
     * this.mockMvc
     * .perform(MockMvcRequestBuilders.get("/admin").header("Authorization",
     * "Bearer " + token))
     * .andExpect(MockMvcResultMatchers.status().isForbidden());
     * }
     *
     * @Test
     * void AdminPageAccess_withPriviledge() throws Exception {
     * MvcResult result = this.mockMvc
     * .perform(
     * MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * // Integer size = token.length();
     * // token = token.substring(8,size-2);
     *
     * this.mockMvc
     * .perform(MockMvcRequestBuilders.get("/admin").header("Authorization",
     * "Bearer " + token))
     * .andExpect(MockMvcResultMatchers.status().isOk());
     * SignOut();
     * }
     *
     * @Test
     * void AdminPageAccess_withoutPriviledge() throws Exception {
     * MvcResult result = this.mockMvc
     * .perform(
     * MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * // Integer size = token.length();
     * // token = token.substring(8,size-2);
     *
     * this.mockMvc
     * .perform(MockMvcRequestBuilders.get("/admin").header("Authorization",
     * "Bearer " + token))
     * .andExpect(MockMvcResultMatchers.status().isForbidden());
     * }
     *
     * @Test
     * void ListUsers_withPriviledge() throws Exception {
     * MvcResult result = this.mockMvc
     * .perform(
     * MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * // Integer size = token.length();
     * // token = token.substring(8,size-2);
     *
     * this.mockMvc
     * .perform(MockMvcRequestBuilders.get("/admin/list")
     * .header("Authorization", "Bearer " + token))
     * .andExpect(MockMvcResultMatchers.status().isOk());
     * SignOut();
     * }
     */

    /*
     * @Test
     * void ListUsers_withoutPriviledge() throws Exception {
     * MvcResult result =
     * this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * System.out.println(token);
     * //Integer size = token.length();
     * //token = token.substring(8,size-2);
     *
     * this.mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
     * .header("Authorization", "Bearer " + token))
     * .andExpect(MockMvcResultMatchers.status().isForbidden());
     * SignOut();
     * }
     */

    /*
     * @Test
     * void AddAdminRole_withoutCredentials_then_withCredentials() throws
     * Exception{
     *
     * MvcResult result =
     * this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * //Integer size = token.length();
     * //token = token.substring(8,size-2);
     *
     * this.mockMvc.perform(MockMvcRequestBuilders.put("/admin/add")
     * .header("Authorization", "Bearer " + token)
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("role", "ADMIN"))
     * .andExpect(MockMvcResultMatchers.status().isForbidden());
     *
     * SignOut();
     *
     * MvcResult login_result =
     * this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = login_result.getResponse().getContentAsString();
     * //Integer size = token.length();
     * //token = token.substring(8,size-2);
     *
     * this.mockMvc.perform(MockMvcRequestBuilders.put("/admin/add")
     * .header("Authorization", "Bearer " + token)
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("role", "ADMIN"))
     * .andExpect(MockMvcResultMatchers.status().isOk());
     * SignOut();
     * }
     */

    /*
     * @Test
     * void ModulePageAccess_withToken() throws Exception {
     * MvcResult result = this.mockMvc
     * .perform(
     * MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * // Integer size = token.length();
     * // token = token.substring(8,size-2);
     *
     * this.mockMvc
     * .perform(MockMvcRequestBuilders.get("/module").header(
     * "Authorization", "Bearer " + token))
     * .andExpect(MockMvcResultMatchers.status().isOk());
     * SignOut();
     * }
     *
     * @Test
     * void ModulePageAccess_withoutToken() throws Exception {
     * this.mockMvc.perform(MockMvcRequestBuilders.get("/module"))
     * .andExpect(MockMvcResultMatchers.status().isUnauthorized());
     * }
     */

    /*
     * @Test
     * void UsersPageAccess_withPriviledge() throws Exception {
     * MvcResult result =
     * this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * //Integer size = token.length();
     * //token = token.substring(8,size-2);
     *
     * this.mockMvc.perform(MockMvcRequestBuilders.get("/users")
     * .header("Authorization", "Bearer " + token))
     * .andExpect(MockMvcResultMatchers.status().isOk());
     * SignOut();
     * }
     */
    /*
     * @Test
     * void UsersPageAccess_withoutPriviledge() throws Exception {
     * MvcResult result =
     * this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * //Integer size = token.length();
     * //token = token.substring(8,size-2);
     *
     * //Delete Tutor role from user
     * this.mockMvc.perform(MockMvcRequestBuilders.delete("/admin/delete")
     * .header("Authorization", "Bearer " + token)
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("role", "TUTOR"))
     * .andExpect(MockMvcResultMatchers.status().isOk());
     *
     * SignOut();
     *
     * MvcResult user_result =
     * this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", USER_email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * String new_token = user_result.getResponse().getContentAsString();
     * //Integer size_ = token.length();
     * //token = token.substring(8,size_-2);
     *
     * this.mockMvc.perform(MockMvcRequestBuilders.get("/users")
     * .header("Authorization", "Bearer " + new_token))
     * .andExpect(MockMvcResultMatchers.status().isForbidden());
     * SignOut();
     * }
     */

    /*
     * @Test
     * void AccountPageAccess_withToken() throws Exception {
     * MvcResult result = this.mockMvc
     * .perform(
     * MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * // Integer size = token.length();
     * // token = token.substring(8,size-2);
     *
     * this.mockMvc
     * .perform(MockMvcRequestBuilders.get("/account")
     * .header("Authorization", "Bearer " + token))
     * .andExpect(MockMvcResultMatchers.status().isOk());
     * SignOut();
     * }
     *
     * @Test
     * void AccountPageAccess_withoutToken() throws Exception {
     * this.mockMvc.perform(MockMvcRequestBuilders.get("/account"))
     * .andExpect(MockMvcResultMatchers.status().isUnauthorized());
     * }
     *
     * @Test
     * void SignOut() throws Exception {
     * MvcResult result = this.mockMvc
     * .perform(
     * MockMvcRequestBuilders.post("/login")
     * .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
     * .param("email", email)
     * .param("password", pw))
     * .andExpect(MockMvcResultMatchers.status().isOk())
     * .andReturn();
     *
     * token = result.getResponse().getContentAsString();
     * // Integer size = token.length();
     * // token = token.substring(8,size-2);
     *
     * this.mockMvc.perform(MockMvcRequestBuilders.post("/signout"))
     * .andExpect(MockMvcResultMatchers.status().isOk());
     *
     * /*
     * this.mockMvc.perform(MockMvcRequestBuilders.get("/account")
     * .header("Authorization", "Bearer " + token))
     * .andExpect(MockMvcResultMatchers.status().isUnauthorized());
     */
}
