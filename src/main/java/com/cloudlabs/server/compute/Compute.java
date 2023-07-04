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

    public Compute() {
    }

    public Compute(String instanceName, String machineType, String ipv4Address) {
        this.instanceName = instanceName;
        this.machineType = machineType;
        this.ipv4Address = ipv4Address;
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

}