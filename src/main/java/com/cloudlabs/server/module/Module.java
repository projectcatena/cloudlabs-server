package com.cloudlabs.server.module;

import com.cloudlabs.server.compute.Compute;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

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

    @OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "module_id", referencedColumnName = "moduleId")
    private Set<Compute> computes = new HashSet<>();
    
    public Module() {}

    public Module(String moduleSubtitle, String moduleName, String moduleDescription) {
            this.moduleSubtitle = moduleSubtitle;
            this.moduleName = moduleName;
            this.moduleDescription = moduleDescription;
        }

    public Module(String moduleSubtitle, String moduleName, String moduleDescription, 
                Set<Compute> computes) {
            this.moduleSubtitle = moduleSubtitle;
            this.moduleName = moduleName;
            this.moduleDescription = moduleDescription;
            this.computes = computes;
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

    public Set<Compute> getComputes() {
        return computes;
    }

    public void setComputes(Set<Compute> computes) {
        this.computes = computes;
    }
}
