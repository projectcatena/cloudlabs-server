package com.cloudlabs.server.file;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import com.google.cloudbuild.v1.Build;

public interface FileService {
    URL generateV4PutObjectSignedUrl(String objectName);
    Boolean checkBlobExist(String blobName);
    Build startVirtualDiskBuild(String objectName, String imageName) throws InterruptedException, ExecutionException, IOException;
    Build cancelVirtualDiskBUild(String buildId) throws IOException;
}
