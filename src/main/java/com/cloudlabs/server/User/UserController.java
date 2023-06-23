package com.cloudlabs.server.user;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RestController
@RequestMapping()
public class UserController {

	private UserService userService;

	//private final UserDetailsService userDetailsService;

	//, UserDetailsService userDetailsService
	public UserController(UserService userService) {
		this.userService = userService;
		//this.userDetailsService = userDetailsService;
	}
	
	//handler method to handle user registration form request
    @GetMapping(path = "signup")
    public String showRegistrationForm(Model model){
        // create model object to store form data
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }
	
	@GetMapping(path = "module")
	@PreAuthorize("hasAuthority('USER')")
	public String getModuleUser(Model model) {
		List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "module";
		/*
		JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
		Map<String, Object> attributes = token.getTokenAttributes();
		return userDetailsService.loadUserByUsername(attributes.get("username").toString());
		 */
	}

	@GetMapping(path = "users")
	@PreAuthorize("hasAuthority('TUTOR')")
	public String listRegisteredUsers(Model model){
		System.out.print("in");
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

	@GetMapping(path = "login")
	public String getLoginPage() {
		return "login";
	}
	
	
}
