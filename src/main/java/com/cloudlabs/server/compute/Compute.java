package com.cloudlabs.server.compute;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="compute")
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

    public Compute() {
    }

    public Compute(String instanceName, String machineType, String ipv4Address, 
    long diskSizeGb, String sourceImage) {
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

}