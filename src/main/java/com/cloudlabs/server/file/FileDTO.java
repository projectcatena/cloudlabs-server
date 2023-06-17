package com.cloudlabs.server.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL) // Ignore null fields on controller response
public class FileDTO {
    private String objectName;
    private String imageName;
    private String signedURL;
    private String buildStatus;
    private String buildId;

    public FileDTO() {}

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

    public String getImageName() {
        return this.imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getSignedURL() {
        return this.signedURL;
    }

    public void setSignedURL(String signedURL) {
        this.signedURL = signedURL;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
    
}
