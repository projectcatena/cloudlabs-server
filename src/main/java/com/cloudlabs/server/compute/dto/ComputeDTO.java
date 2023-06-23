package com.cloudlabs.server.compute.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL) // Ignore null fields on controller response
public class ComputeDTO {
    private SourceImageDTO sourceImage;
    private MachineTypeDTO machineType;
    private long diskSizeGb;
    private String networkName;
    private String instanceName;
    private String startupScript;
    private AddressDTO addressDTO;
    private String status;

    public ComputeDTO() {}

    public SourceImageDTO getSourceImage() {
        return this.sourceImage;
    }

    public void setSourceImage(SourceImageDTO sourceImage) {
        this.sourceImage = sourceImage;
    }

    public MachineTypeDTO getMachineType() {
        return this.machineType;
    }

    public void setMachineType(MachineTypeDTO machineType) {
        this.machineType = machineType;
    }

    public long getDiskSizeGb() {
        return this.diskSizeGb;
    }

    public void setDiskSizeGb(long diskSizeGb) {
        this.diskSizeGb = diskSizeGb;
    }

    public String getNetworkName() {
        return this.networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getStartupScript() {
        return this.startupScript;
    }

    public void setStartupScript(String startupScript) {
        this.startupScript = startupScript;
    }

    public AddressDTO getAddressDTO() {
        return this.addressDTO;
    }

    public void setAddressDTO(AddressDTO addressDTO) {
        this.addressDTO = addressDTO;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
