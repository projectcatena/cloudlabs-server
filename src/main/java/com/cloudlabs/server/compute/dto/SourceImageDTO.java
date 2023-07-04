package com.cloudlabs.server.compute.dto;

public class SourceImageDTO {
    private String name;
    private String project;

    public SourceImageDTO() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProject() {
        return this.project;
    }

    public void setProject(String project) {
        this.project = project;
    }

}
