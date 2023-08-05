package com.cloudlabs.server.snapshot;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="snapshot")
public class SaveSnapshot {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "name", nullable = false)
    private String snapshotName;

    @Column(name = "description", length = 150 ,nullable = true)
    private String description;

    public SaveSnapshot() {}

    public SaveSnapshot(String snapshotName, String description) {
        this.snapshotName = snapshotName;
        this.description = description;
    }

    public String getSnapshotName() {
        return this.snapshotName;
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
