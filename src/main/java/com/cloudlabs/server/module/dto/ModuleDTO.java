package com.cloudlabs.server.module.dto;

import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.user.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_DEFAULT)
// Ignore null fields, and default values (like 0 for long type)
public class ModuleDTO {

    private Long id;
    private String moduleSubtitle;
    private String moduleName;
    private String moduleDescription;
    private List<ComputeDTO> computes;
    private List<UserDTO> users;

    public ModuleDTO() {
    };

    public Long getModuleId() {
        return this.id;
    }

    public void setModuleId(Long id) {
        this.id = id;
    }

    public String getModuleSubtitle() {
        return this.moduleSubtitle;
    }

    public void setModuleSubtitle(String moduleSubtitle) {
        this.moduleSubtitle = moduleSubtitle;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleDescription() {
        return this.moduleDescription;
    }

    public void setModuleDescription(String moduleDescription) {
        this.moduleDescription = moduleDescription;
    }

    public List<ComputeDTO> getComputes() {
        return computes;
    }

    public void setComputes(List<ComputeDTO> computes) {
        this.computes = computes;
    }

    public List<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }
}
