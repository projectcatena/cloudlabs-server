package com.cloudlabs.server.user.dto;

import com.cloudlabs.server.compute.Compute;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.module.dto.ModuleDTO;
import com.cloudlabs.server.role.RoleType;
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
    private RoleType newRole;
    private List<ComputeDTO> computes;
    private List<ModuleDTO> modules;

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
            String currentPassword, String newPassword,
            List<RoleDTO> roles, RoleType newRole) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.roles = roles;
        this.newRole = newRole;
    }

    public UserDTO(String fullname, String username, String email,
            String currentPassword, List<RoleDTO> roles) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.currentPassword = currentPassword;
        this.roles = roles;
    }

    public UserDTO(String fullname, String username, String email, List<ComputeDTO> computes) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.computes = computes;
            }

    public UserDTO(String fullname, String username, String email, List<ModuleDTO> modules, boolean isModuleConstructor) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.modules = modules;
            }

    public String getFullname() {
        return this.fullname;
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
        return this.roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }

    public RoleType getNewRole() {
        return this.newRole;
    }

    public void setNewRole(String newRole) {
        this.newRole = RoleType.valueOf(newRole.toUpperCase());
    }

    public List<ComputeDTO> getComputes() {
        return this.computes;
    }

    public void setComputes(List<ComputeDTO> computes) {
        this.computes = computes;
    }

    public List<ModuleDTO> getModules() {
        return this.modules;
    }

    public void setModules(List<ModuleDTO> modules) {
        this.modules = modules;
    }
}
