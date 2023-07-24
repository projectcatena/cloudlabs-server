package com.cloudlabs.server.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class UserDTO {
    
    private String fullName;
    private String username;
    private String email;
    private String currentPassword;
    private String newPassword;

    public UserDTO() {}

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullname) {
        this.fullName = fullname;
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
    
}
