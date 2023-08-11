package com.cloudlabs.server.compute.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL) // Ignore null fields on controller response
public class AddressDTO {
 private String subnetName;
 private String privateIPv4Address;

 public String getSubnetName() {
  return subnetName;
 }

 public void setSubnetName(String subnetName) {
  this.subnetName = subnetName;
 }

 public String getPrivateIPv4Address() {
  return privateIPv4Address;
 }

 public void setPrivateIPv4Address(String privateIPv4Address) {
  this.privateIPv4Address = privateIPv4Address;
 }
}
