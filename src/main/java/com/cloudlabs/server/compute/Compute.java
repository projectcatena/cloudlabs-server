package com.cloudlabs.server.compute;

import com.cloudlabs.server.user.User;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "compute")
public class Compute {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "instance_name", nullable = false)
    private String instanceName;

    @Column(name = "machine_type", nullable = false)
    private String machineType;

    @Column(name = "external_ip_address", nullable = false)
    private String ipv4Address;

    @Column(name = "disk_size_in_GB", nullable = false)
    private long diskSizeGb;

    @Column(name = "source_image", nullable = false)
    private String sourceImage;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "computes_users", joinColumns = @JoinColumn(name = "compute_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "user_email", referencedColumnName = "email"))
    private Set<User> users = new HashSet<>();

    public Compute() {
    }

    public Compute(String instanceName, String machineType, String ipv4Address) {
        this.instanceName = instanceName;
        this.machineType = machineType;
        this.ipv4Address = ipv4Address;
    }

    public Compute(String instanceName, String machineType, String ipv4Address,
            long diskSizeGb, String sourceImage) {
        this.instanceName = instanceName;
        this.machineType = machineType;
        this.ipv4Address = ipv4Address;
        this.diskSizeGb = diskSizeGb;
        this.sourceImage = sourceImage;
    }

    public Compute(String instanceName, String machineType, String ipv4Address,
            Set<User> users) {
        this.instanceName = instanceName;
        this.machineType = machineType;
        this.ipv4Address = ipv4Address;
        this.users = users;
    }

    public Compute(String instanceName, String machineType, String ipv4Address,
            long diskSizeGb, String sourceImage, Set<User> users) {
        this.instanceName = instanceName;
        this.machineType = machineType;
        this.ipv4Address = ipv4Address;
        this.diskSizeGb = diskSizeGb;
        this.sourceImage = sourceImage;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getMachineType() {
        return this.machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getIpv4Address() {
        return this.ipv4Address;
    }

    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public long getDiskSizeGb() {
        return this.diskSizeGb;
    }

    public void setDiskSizeGb(long diskSizeGb) {
        this.diskSizeGb = diskSizeGb;
    }

    public String getSourceImage() {
        return this.sourceImage;
    }

    public void setSourceImage(String sourceImage) {
        this.sourceImage = sourceImage;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
