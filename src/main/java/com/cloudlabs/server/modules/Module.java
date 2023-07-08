package com.cloudlabs.server.modules;

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

    public Module() {}

    public Module(String moduleSubtitle, String moduleName, String moduleDescription) {
            this.moduleSubtitle = moduleSubtitle;
            this.moduleName = moduleName;
            this.moduleDescription = moduleDescription;
        }

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
