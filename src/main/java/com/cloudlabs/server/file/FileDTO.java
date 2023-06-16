package com.cloudlabs.server.file;

public class FileDTO {
    private String objectName;
    private String signedURL;

    public FileDTO() {
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
