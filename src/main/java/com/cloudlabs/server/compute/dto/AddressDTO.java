package com.cloudlabs.server.compute.dto;

enum AddressStatus {

}

public class AddressDTO {
   private String name;
   private String ipv4Address;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpv4Address() {
        return this.ipv4Address;
    }

    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }
}
