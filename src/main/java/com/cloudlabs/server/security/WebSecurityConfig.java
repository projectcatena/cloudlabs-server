package com.cloudlabs.server.security;

import com.cloudlabs.server.security.jwt.JwtAuthenticationFilter;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    public static final String AUTHORITIES_CLAIM_NAME = "roles";

    /*
     * Apply the security filters by chaining them.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        /*
         * http.csrf().disable()
         * .authorizeHttpRequests((authorize) ->
         * authorize.requestMatchers("/module").hasRole)
         */

        http.cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll() // allow CORS option call
                .antMatchers("/auth/login", "/auth/signout", "/auth/register", "/error")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        // JWT Validation Configuration
        /*
         * http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(
         * authenticationConverter());
         */

        /*
         * .formLogin()
         * .permitAll()
         * .successHandler(successHandler)
         * .and()
         */
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
        configuration.setExposedHeaders(Arrays.asList("Guacamole-Status-Code",
                "Guacamole-Error-Message",
                "Guacamole-Tunnel-Token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Converts Bearer token to Jwt token
    /*
     * @Bean
     * protected JwtAuthenticationConverter authenticationConverter() {
     * JwtGrantedAuthoritiesConverter authoritiesConverter = new
     * JwtGrantedAuthoritiesConverter();
     * authoritiesConverter.setAuthorityPrefix("");
     * authoritiesConverter.setAuthoritiesClaimName(AUTHORITIES_CLAIM_NAME);
     *
     * JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
     * converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
     * return converter;
     * }
     */
}
