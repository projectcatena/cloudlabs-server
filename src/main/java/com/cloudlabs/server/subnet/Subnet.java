package com.cloudlabs.server.subnet;

import com.cloudlabs.server.compute.Compute;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "network")
public class Subnet {

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "subnet_name", nullable = false)
    private String subnetName;

    @Column(name = "ipv4_range", nullable = false)
    private String ipv4Range;
    
    @Column(name = "firewall_rule_name")
    private String firewallRuleName;

    @OneToMany(mappedBy = "subnet")
    private Set<Compute> computes;

    public Subnet() {
    }

    public Subnet(String subnetName, String ipv4Range) {
        this.subnetName = subnetName;
        this.ipv4Range = ipv4Range;
    }

    public Subnet(String subnetName, String ipv4Range, String firewallRuleName) {
        this.subnetName = subnetName;
        this.ipv4Range = ipv4Range;
        this.firewallRuleName = firewallRuleName;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubnetName() {
        return this.subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public String getIpv4Range() {
        return this.ipv4Range;
    }

    public void setIpv4Range(String ipv4Range) {
        this.ipv4Range = ipv4Range;
    }

    public String getFirewallRuleName() {
        return this.firewallRuleName;
    }

    public void setFirewallRuleName(String firewallRuleName) {
        this.firewallRuleName = firewallRuleName;
    }
}
