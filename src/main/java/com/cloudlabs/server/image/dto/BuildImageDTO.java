package com.cloudlabs.server.image.dto;

public class BuildImageDTO extends ImageDTO {
    private String buildStatus;
    private String buildId;

    public String getBuildStatus() {
        return this.buildStatus;
    }

    public void setBuildStatus(String buildStatus) {
        this.buildStatus = buildStatus;
    }

    public String getBuildId() {
        return this.buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

}
