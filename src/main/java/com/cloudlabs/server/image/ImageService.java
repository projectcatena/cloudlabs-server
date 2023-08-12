package com.cloudlabs.server.image;

import com.cloudlabs.server.image.dto.BuildImageDTO;
import com.cloudlabs.server.image.dto.DeleteImageDTO;
import com.cloudlabs.server.image.dto.ImageDTO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ImageService {
    ImageDTO generateV4PutObjectSignedUrl(String objectName,
            String operatingSystem);

    Boolean checkBlobExist(String blobName);

    BuildImageDTO startVirtualDiskBuild(String objectName, String imageName)
            throws InterruptedException, ExecutionException, IOException;

    BuildImageDTO cancelVirtualDiskBUild(String buildId) throws IOException;

    List<ImageDTO> listImages() throws IOException;

    DeleteImageDTO deleteImage(String imageName)
            throws IOException, InterruptedException, ExecutionException;
}
