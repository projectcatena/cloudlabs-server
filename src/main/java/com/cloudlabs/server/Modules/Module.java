package com.cloudlabs.server.Modules;

import java.util.UUID;

public class Module {
    private String moduleId;
    private String moduleSubtitle;
    private String moduleName;
    private String moduleDescription;

    public String getModuleID() {
        return this.moduleId;
    }

    public void setModuleId(String moduleId) {
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
    
    public static Module from(String moduleSubtitle, String moduleName, String moduleDescription) {
        Module module = new Module();
        module.setModuleSubtitle(moduleSubtitle);
        module.setModuleName(moduleName);
        module.setModuleDescription(moduleDescription);

        module.setModuleId(UUID.randomUUID().toString());
        return module;
    }
}
