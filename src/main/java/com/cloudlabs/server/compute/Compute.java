package com.cloudlabs.server.compute;

import com.cloudlabs.server.module.Module;
import com.cloudlabs.server.subnet.Subnet;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "compute")
public class Compute {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "instance_name", nullable = false, unique = true)
    private String instanceName;

    @Column(name = "machine_type", nullable = false)
    private String machineType;

    @Column(name = "private_ip_address", nullable = false, unique = true)
    private String privateIPv4Address;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "computes_users", joinColumns = @JoinColumn(name = "compute_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "user_email", referencedColumnName = "email"))
    private Set<User> users = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "subnet_id", referencedColumnName = "id", nullable = false)
    private Subnet subnet;

    @ManyToOne
    private Module module;

    public Compute() {
    }

    public Compute(String instanceName, String machineType,
            String privateIPv4Address) {
        this.instanceName = instanceName;
        this.machineType = machineType;
        this.privateIPv4Address = privateIPv4Address;
    }

    public Compute(String instanceName, String machineType,
            String privateIPv4Address, Set<User> users) {
        this.instanceName = instanceName;
        this.machineType = machineType;
        this.privateIPv4Address = privateIPv4Address;
        this.users = users;
    }

    public Compute(String instanceName, String machineType,
            String privateIPv4Address, Set<User> users, Subnet subnet) {
        this.instanceName = instanceName;
        this.machineType = machineType;
        this.privateIPv4Address = privateIPv4Address;
        this.users = users;
        this.subnet = subnet;
    }

    public Compute(String instanceName, String machineType,
            String privateIPv4Address, Set<User> users, Subnet subnet,
            Module module) {
        this.instanceName = instanceName;
        this.machineType = machineType;
        this.privateIPv4Address = privateIPv4Address;
        this.users = users;
        this.subnet = subnet;
        this.module = module;
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

    public String getPrivateIPv4Address() {
        return privateIPv4Address;
    }

    public void setPrivateIPv4Address(String privateIPv4Address) {
        this.privateIPv4Address = privateIPv4Address;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Subnet getSubnet() {
        return subnet;
    }

    public void setSubnet(Subnet subnet) {
        this.subnet = subnet;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }
}
