package com.cloudlabs.server.snapshot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;;

@JsonInclude(Include.NON_DEFAULT) // Ignore null fields, and default values (like 0 for long type)
public class SaveSnapshotDto {

    private String snapshotName;
    private String description;

    public SaveSnapshotDto() {}

    public SaveSnapshotDto(String snapshotName, String description) {
        this.snapshotName = snapshotName;
        this.description = description;
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

}

