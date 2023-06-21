package com.cloudlabs.server.modules;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "modules")
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long moduleId;

    @Column(name = "subtitle", nullable = false)
    private String moduleSubtitle;

    @Column(name = "name", nullable = false)
    private String moduleName;

    @Column(name = "description", length = 1000, nullable = false)
    private String moduleDescription;

    public Long getModuleID() {
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
    
    public static Module from(String moduleSubtitle, String moduleName, String moduleDescription) {
        Module module = new Module();
        module.setModuleSubtitle(moduleSubtitle);
        module.setModuleName(moduleName);
        module.setModuleDescription(moduleDescription);

        module.setModuleId(Long.valueOf(UUID.randomUUID().toString()));

        return module;
    }
}
