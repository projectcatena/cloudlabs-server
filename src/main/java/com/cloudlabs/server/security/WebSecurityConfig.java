package com.cloudlabs.server.security;

import com.cloudlabs.server.security.jwt.JwtAuthenticationFilter;
import com.cloudlabs.server.user.UserRepository;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
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
    private UserRepository userRepository;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
                .antMatchers("/login", "/signout", "/signup", "/error")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider())
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

    // Override the loadByUsername method to find user by email
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found"));
    }

    /*
     * This is the data access object that is responsible for fetching user
     * details and encode password .etc
     *
     * When authentication is successful, the Authentication that is returned is
     * of type UsernamePasswordAuthenticationToken and has a principal that is the
     * UserDetails returned by the configured UserDetailsService. Ultimately, the
     * returned UsernamePasswordAuthenticationToken is set on the
     * SecurityContextHolder by the authentication Filter.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // An implementation of an Authentication Provider that uses a
        // UserDetailsService and PasswordEncoder to authenticate a username and
        // password.
        // Source:
        // https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

        // DaoAuthenticationProvider looks up the UserDetails from the
        // UserDetailsService.
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());

        // DaoAuthenticationProvider then uses the PasswordEncoder to validate the
        // password on the UserDetails returned in the previous step.
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Literally manages the authentication
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
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
