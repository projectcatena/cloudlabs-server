 package com.cloudlabs.server.admin;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.user.dto.UserDTO;

@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RestController()
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;


    @GetMapping(path = "/list")
    @PreAuthorize("hasRole('ADMIN')") //remove user & tutor
    public List<UserDTO> listUsers() throws IOException {
        List<UserDTO> userList = adminService.getAllUsers();
        if (userList == null) {
            return null;
        }
        return userList;
    }

    @PutMapping(path = "/add")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO addRole(@RequestBody UserDTO userDTO) throws IOException {
        UserDTO result = adminService.setNewRole(userDTO);
        if (result.equals(null)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to add role");
        }
        return result;
    }

    @DeleteMapping(path = "/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO deleteRole(@RequestBody UserDTO userDTO) throws IOException {
        UserDTO result = adminService.deleteRole(userDTO);
        return result;
    }
}

