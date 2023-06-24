package com.cloudlabs.server.security;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.WebSecurityConfig;
import com.cloudlabs.server.security.resource.LoginResult;
import com.cloudlabs.server.user.CustomUserDetailsService;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserDto;
import com.cloudlabs.server.user.UserService;

/**
 * The auth controller to handle login requests
 *
 * @author imesha
 */
@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RestController
public class AuthController {
	
	private final JwtHelper jwtHelper;
	//private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private UserService userService;
	private CustomUserDetailsService customUserDetailsService;
	
	public AuthController(JwtHelper jwtHelper,
			PasswordEncoder passwordEncoder,
			UserService userService,
			CustomUserDetailsService customUserDetailsService) {
		this.jwtHelper = jwtHelper;
		//this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
		this.customUserDetailsService = customUserDetailsService;
	}
	
	// handler method to handle user login request
	@PostMapping(path = "login", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
	public LoginResult login(
			@RequestParam String email,
			@RequestParam String password) {
		
		//UserDetails userDetails;
		try {
			UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
			User user = userService.findByEmail(email); // userRepository.findbyemail(email)

			if (passwordEncoder.matches(password, user.getPassword())) {
			Map<String, String> claims = new HashMap<>();
			claims.put("email", email);
			claims.put("sub", user.getName());
			
			String roles = userDetails.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(" "));
			claims.put(WebSecurityConfig.AUTHORITIES_CLAIM_NAME, roles);
			claims.put("userId", "" + user.getId());
			String jwt = jwtHelper.createJwtForClaims(email, claims);
			
			return new LoginResult(jwt);
			}
		} catch (UsernameNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
		}

		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
	}

	// handler method to handle register user form submit request
    @PostMapping(path = "signup", consumes = (MediaType.APPLICATION_FORM_URLENCODED_VALUE))
    public String registration(
		@RequestParam String fullName,
		@RequestParam String email,
		@RequestParam String password
	){
		User existing = userService.findByEmail(email);
        if (existing != null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account already exists");
        }
		UserDto userDto = new UserDto();
		userDto.setName(fullName);
		userDto.setEmail(email);
		userDto.setPassword(password);
        
        userService.saveUser(userDto);
        return "Redirect:/login";
    }
}