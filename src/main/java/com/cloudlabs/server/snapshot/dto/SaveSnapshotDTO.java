package com.cloudlabs.server.snapshot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;;

@JsonInclude(Include.NON_DEFAULT) // Ignore null fields, and default values (like 0 for long type)
public class SaveSnapshotDTO {

    private String snapshotName;
    private String description;
    private String instanceName;

    public SaveSnapshotDTO() {}

    public SaveSnapshotDTO(String snapshotName, String description, String instanceName) {
        this.snapshotName = snapshotName;
        this.description = description;
        this.instanceName = instanceName;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

}

