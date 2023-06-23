package com.cloudlabs.server.compute.dto;

enum AddressStatus {

}

public class AddressDTO {
   private String externalIPv4Address;


    public String getExternalIPv4Address() {
        return this.externalIPv4Address;
    }

    public void setExternalIPv4Address(String externalIPv4Address) {
        this.externalIPv4Address = externalIPv4Address;
    }

}
