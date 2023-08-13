package com.cloudlabs.server.snapshot;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.cloudlabs.server.user.User;

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

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    //@ManyToOne
    //@JoinColumn(name = "compute_id", referencedColumnName = "id", nullable = false)
    @Column
    private String instanceName;

    public SaveSnapshot() {}

    public SaveSnapshot(String snapshotName, String description, User user, String instanceName) {
        this.snapshotName = snapshotName;
        this.description = description;
        this.user = user;
        this.instanceName = instanceName;
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

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

}
