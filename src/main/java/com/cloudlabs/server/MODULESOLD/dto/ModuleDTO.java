package com.cloudlabs.server.MODULESOLD.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT) // Ignore null fields, and default values (like 0 for long type)
public class ModuleDTO {

    private Long moduleId;
    private String moduleSubtitle;
    private String moduleName;
    private String moduleDescription;

    public ModuleDTO() {};

    public Long getModuleId() {
        return this.moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
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
}
