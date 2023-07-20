package com.cloudlabs.server.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.user.dto.UserDTO;

@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RestController
@RequestMapping("account")
public class UserController {
    
    @Autowired
    UserService userService;

    @GetMapping("get/{userId}")
    @PreAuthorize("hasAnyRole('USER','TUTOR','ADMIN')")
    public UserDTO getUserDetails(@PathVariable String userId) throws Exception {
        Long id = Long.valueOf(userId);
        UserDTO result = userService.getUserDetails(id);
        if (result.getEmail() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return result;
    }

    @PostMapping("update")
    @PreAuthorize("hasAnyRole('USER','TUTOR','ADMIN')")
    public UserDTO updateUserDetails(@RequestBody UserDTO userDTO) throws Exception {
        UserDTO result = userService.updateUserDetails(userDTO);
        
        if (result.equals(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to retrieve");
        }
        return result;
    }
}
