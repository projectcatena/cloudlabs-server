package com.cloudlabs.server.subnet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT) // Ignore null fields, and default values (like 0 for long type)
public class SubnetDTO {
    private String subnetName;
    private String ipv4Range;
    private String status;

    public SubnetDTO() {}

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

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
