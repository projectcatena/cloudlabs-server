package com.cloudlabs.server.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.user.CustomUserDetailsService;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserDto;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.UserService;

/**
 * The auth controller to handle login requests
 *
 * @author imesha
 */
@CrossOrigin(origins = { "${app.security.cors.origin}" }, allowCredentials = "true")
@RestController()
@RequestMapping
public class AuthController {

	private final JwtHelper jwtHelper;
	// private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private UserService userService;
	private CustomUserDetailsService customUserDetailsService;
	private UserRepository userRepository;
	private AuthService authService;

	public AuthController(JwtHelper jwtHelper, PasswordEncoder passwordEncoder,
			UserService userService,
			CustomUserDetailsService customUserDetailsService,
			UserRepository userRepository,
			AuthService authService) {
		this.jwtHelper = jwtHelper;
		// this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
		this.customUserDetailsService = customUserDetailsService;
		this.userRepository = userRepository;
		this.authService = authService;
	}

	// handler method to handle user login request
	@PostMapping(path = "login", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
	public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password,
			HttpServletResponse response) {

		// UserDetails userDetails;
		try {
			UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
			User user = userService.findByEmail(email); // userRepository.findbyemail(email)

			if (passwordEncoder.matches(password, user.getPassword())) {
				String jwt = authService.createJwtToken(email, user, userDetails);

				final Cookie cookie = new Cookie("jwt", jwt);
				cookie.setHttpOnly(true);
				cookie.setPath("/");
				cookie.setDomain("localhost");
				cookie.setMaxAge(1800); // 30 minutes per session

				response.addCookie(cookie);
				return new ResponseEntity<String>("Status OK", HttpStatus.OK);
			}
		} catch (UsernameNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"User not found");
		}

		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
				"User not authenticated");
	}

	// handler method to handle register user form submit request
	@PostMapping(path = "signup", consumes = (MediaType.APPLICATION_FORM_URLENCODED_VALUE))
	public ResponseEntity<String> registration(@RequestParam String name, @RequestParam String email,
			@RequestParam String password) {
		User existing = userService.findByEmail(email);
		if (existing != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Account already exists");
		}
		UserDto userDto = authService.createNewUser(name, email, password);
		userService.saveUser(userDto);
		return ResponseEntity.ok("Successful Registration");
	}
}
