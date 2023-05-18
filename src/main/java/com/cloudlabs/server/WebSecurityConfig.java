package com.cloudlabs.server;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // http.authorizeHttpRequests((requests) -> requests
        //     .requestMatchers("/", "/tunnel").permitAll()
        //     .anyRequest().anonymous()
        // );

        // Adds a CorsFilter to be used. If a bean by the name of corsFilter is provided, that CorsFilter is used. 
        // Else if corsConfigurationSource is defined, then that CorsConfiguration is used.
        http.cors().and().csrf().disable();
        http.httpBasic().disable();

        return http.build();
    }

    // CORS Configuration for Apache Guacamole Traffic
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Guacamole-Status-Code", "Guacamole-Error-Message", "Guacamole-Tunnel-Token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
