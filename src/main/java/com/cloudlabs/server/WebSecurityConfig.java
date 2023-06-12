package com.cloudlabs.server;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


//@EnableWebSecurity
@Component
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
	private final PasswordEncoder passwordEncoder;
	
	public WebSecurityConfig(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.cors()
				.and()
				.csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.authorizeRequests(configurer ->
						configurer
								.antMatchers(
										"/error",
										"/login"
								)
								.permitAll()
								.anyRequest()
								.authenticated()
				)
				.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
	}
	
	/* Real credentials take from database */
	@Bean
	@Override
	protected UserDetailsService userDetailsService() {
		UserDetails user1 = org.springframework.security.core.userdetails.User
				.withUsername("user")
				.authorities("USER")
				.passwordEncoder(passwordEncoder::encode)
				.password("1234")
				.build();
		
		UserDetails user2 = org.springframework.security.core.userdetails.User
		.withUsername("user2")
		.authorities("ADMIN")
		.passwordEncoder(passwordEncoder::encode)
		.password("1234")
		.build();
		
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		manager.createUser(user1);
		manager.createUser(user2);
		return manager;
	}
	/*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        // http.authorizeHttpRequests((requests) -> requests
        //     .requestMatchers("/", "/tunnel").permitAll()
        //     .anyRequest().anonymous()
        // );

        // Adds a CorsFilter to be used. If a bean by the name of corsFilter is provided, that CorsFilter is used. 
        // Else if corsConfigurationSource is defined, then that CorsConfiguration is used.
        http.cors().and().csrf().disable();
        //http.httpBasic();

        return http.build();
    }
	 */
	
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
