package com.cloudlabs.server.compute.dto;

public class MachineTypeDTO {
    private String name;
    private String zone;

    public MachineTypeDTO() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZone() {
        return this.zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    
}
