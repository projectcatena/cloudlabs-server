package com.cloudlabs.server.user.dto;

import com.cloudlabs.server.role.dto.RoleDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_DEFAULT)
public class UserDTO {

    private String fullname;
    private String username;
    private String email;
    private String currentPassword;
    private String newPassword;

    private List<RoleDTO> roles;

    public UserDTO() {
    }

    public UserDTO(String fullname, String username, String email,
            String currentPassword) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.currentPassword = currentPassword;
    }

    public UserDTO(String fullname, String username, String email,
            String currentPassword, List<RoleDTO> roles) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.currentPassword = currentPassword;
        this.roles = roles;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrentPassword() {
        return this.currentPassword;
    }

    public void setCurrentPassword(String current_password) {
        this.currentPassword = current_password;
    }

    public String getNewPassword() {
        return this.newPassword;
    }

    public void setNewPassword(String new_password) {
        this.newPassword = new_password;
    }

    public List<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }
}
