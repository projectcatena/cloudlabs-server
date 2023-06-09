package com.cloudlabs.server.GCP.compute;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AddressHelperTest {

    @Autowired
    protected MockMvc mockMvc;

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
        .options(WireMockConfiguration.wireMockConfig().dynamicPort())
        .build();

    // @DynamicPropertySource
    // static void configureProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
    //     dynamicPropertyRegistry.add("gcp_base_url", wireMockServer::baseUrl);
    // }

    @Test
    void reserveStaticExternalIPAddress_whenParametersGiven() throws Exception {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/compute/create"))
            .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            .withBody("""
            {
                \"status\": \"success\",
            }
            """)));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/compute/create")
            .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                        \"name\": \"test\",
                        \"selectedImage\": {
                            \"name\": \"debian-11\",
                            \"project\": \"projects/debian-cloud/global/images/family/\"
                        },
                        \"selectedInstanceType\": {
                            \"name\": \"e2-micro\"
                        }
                    }
                    """))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
