package com.cloudlabs.server;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@Component
@EnableGlobalMethodSecurity(
        prePostEnabled = true
)
public class WebSecurityConfig{

	public static final String AUTHORITIES_CLAIM_NAME = "roles";
	
	//private final PasswordEncoder passwordEncoder;

	//@Autowired
    //private AuthenticationSuccessHandlerImpl successHandler;

	//@Autowired
	//private WebApplicationContext applicationContext;

	//@Autowired
    //private DataSource dataSource;

	//private UserDetailsService userDetailsService;
	
	/*
	public WebSecurityConfig(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
	 */
	/*
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
				);
		// JWT Validation Configuration
        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(authenticationConverter());
	}
	 */
	
	/*
	@PostConstruct
	public void completeSetup() {
		userDetailsService = applicationContext.getBean(UserDetailsService.class);
	}
	 */
	/*
	@Bean
    public UserDetailsManager users(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder)
            .and()
            .build();

        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
        jdbcUserDetailsManager.setAuthenticationManager(authenticationManager);
        return jdbcUserDetailsManager;
    }
 */
	/* Real credentials take from database 
	@Bean
	@Override
	protected UserDetailsService userDetailsService() {
		UserDetails user1 = org.springframework.security.core.userdetails.User
				.withUsername("user1@gmail.com")
				.authorities("USER")
				.passwordEncoder(passwordEncoder::encode)
				.password("1234")
				.build();
		
		UserDetails user2 = org.springframework.security.core.userdetails.User
		.withUsername("admin@gmail.com")
		.authorities("ADMIN", "USER")
		.passwordEncoder(passwordEncoder::encode)
		.password("1234")
		.build();

		UserDetails user3 = org.springframework.security.core.userdetails.User
		.withUsername("tutor@gmail.com")
		.authorities("TUTOR", "USER")
		.passwordEncoder(passwordEncoder::encode)
		.password("1234")
		.build();
		
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		manager.createUser(user1);
		manager.createUser(user2);
		manager.createUser(user3);
		return manager;
	}
	*/
	
	
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		/*
		http.csrf().disable()
		.authorizeHttpRequests((authorize) ->
		authorize.requestMatchers("/module").hasRole)
		*/

        http.authorizeRequests()
            .antMatchers("/login", "/signup", "/error")
            .permitAll()
			.anyRequest()
			.authenticated()
            .and()
            .csrf()
            .disable();
		// JWT Validation Configuration
        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(authenticationConverter());

		/*  .formLogin()
            .permitAll()
            .successHandler(successHandler)
            .and()	*/
		return http.build();
    }
	
	//Converts Bearer token to Jwt token
	@Bean
	protected JwtAuthenticationConverter authenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        authoritiesConverter.setAuthoritiesClaimName(AUTHORITIES_CLAIM_NAME);

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
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
