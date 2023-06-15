package com.cloudlabs.server.compute;

public class Compute {
    private String machineType;
    private String sourceImage;
    private long diskSizeGb;
    private String networkName;
    private String instanceName;
    private String startupScript;

    public Compute() {}

    public Compute(String machineType, String sourceImage, long diskSizeGb, String networkName, String instanceName, String startupScript) {
        this.machineType = machineType;
        this.sourceImage = sourceImage;
        this.diskSizeGb = diskSizeGb;
        this.networkName = networkName;
        this.instanceName = instanceName;
        this.startupScript = startupScript;
    }

    public String getMachineType() {
        return this.machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getSourceImage() {
        return this.sourceImage;
    }

    public void setSourceImage(String sourceImage) {
        this.sourceImage = sourceImage;
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

}
