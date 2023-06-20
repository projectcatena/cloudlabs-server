package com.cloudlabs.server.image;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.cloudbuild.v1.Build;

public interface ImageService {
    URL generateV4PutObjectSignedUrl(String objectName);
    Boolean checkBlobExist(String blobName);
    Build startVirtualDiskBuild(String objectName, String imageName) throws InterruptedException, ExecutionException, IOException;
    Build cancelVirtualDiskBUild(String buildId) throws IOException;
    List<ImageDTO> listImages() throws IOException;
    void deleteImage(String imageName) throws IOException, InterruptedException, ExecutionException;
}
