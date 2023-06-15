package com.cloudlabs.server.file;

import java.net.URL;

public interface FileService {
    // void uploadFile(String contents, String fileName) throws Exception;
    URL generateV4PutObjectSignedUrl(String objectName);
}
