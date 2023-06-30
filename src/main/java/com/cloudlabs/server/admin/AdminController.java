package com.cloudlabs.server.admin;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudlabs.server.role.ERole;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserDto;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.UserService;

@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RequestMapping()
@RestController()
public class AdminController {
    
    private UserService userService;
	private UserRepository userRepository;

	public AdminController(
        UserService userService,
        UserRepository userRepository) {
		this.userService = userService;
		this.userRepository = userRepository;
	}
	
	@GetMapping(path = "admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminPage() {
        return "admin";
    }

	@GetMapping(path = "admin/list")
	//@PreAuthorize("hasAnyAuthority('USER','TUTOR','ADMIN')") //remove user & tutor
	public List<UserDto> userList() throws IOException {
		List<UserDto> userList = userService.findAllUsers();
		if (userList == null) {
			return null;
		}
		return userList;
	}

    @PutMapping(path = "admin/add", consumes = (MediaType.APPLICATION_FORM_URLENCODED_VALUE))
	@PreAuthorize("hasAuthority('ADMIN')")
	public User addRole(
		@RequestParam String email,
		@RequestParam String role
		) throws IOException {
		User user = userRepository.findByEmail(email);
		ERole eRole = ERole.valueOf(role.toUpperCase());
		userService.setNewRole(eRole, user); //check for duplicate role
		return user;
	}

	@DeleteMapping(path = "admin/delete", consumes = (MediaType.APPLICATION_FORM_URLENCODED_VALUE))
	@PreAuthorize("hasAuthority('ADMIN')")
	public User deleteRole(
		@RequestParam String email,
		@RequestParam String role
	) throws IOException {
		User user = userRepository.findByEmail(email);
		ERole eRole = ERole.valueOf(role.toUpperCase());
		userService.deleteRole(eRole, user);
		return user;
	}
}
