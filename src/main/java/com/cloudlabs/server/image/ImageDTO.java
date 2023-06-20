package com.cloudlabs.server.image;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

enum ImageStatus {
    READY,
    FAILED,
    PENDING
}

@JsonInclude(Include.NON_NULL) // Ignore null fields on controller response
public class ImageDTO {
    private String objectName;
    private long imageId;
    private String imageName;
    /**
     * The status of the image. An image can be used to create other resources, 
     * such as instances, only after the image has been successfully created 
     * and the status is set to READY. Possible values are FAILED, PENDING, or READY.
     */
    private ImageStatus imageStatus;
    private String signedURL;
    private String buildStatus;
    private String buildId;
    private Instant creationTimestamp;

    public ImageDTO() {}

    public Instant getCreationTimestamp() {
        return this.creationTimestamp;
    }

    public void setCreationTimestamp(Instant creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public long getImageId() {
        return this.imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
    }

    public ImageStatus getImageStatus() {
        return this.imageStatus;
    }

    public void setImageStatus(ImageStatus imageStatus) {
        this.imageStatus = imageStatus;
    }

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
