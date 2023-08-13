package com.cloudlabs.server.image;

import com.cloudlabs.server.image.dto.BuildImageDTO;
import com.cloudlabs.server.image.dto.DeleteImageDTO;
import com.cloudlabs.server.image.dto.ImageDTO;
import com.cloudlabs.server.image.enums.DeleteImageStatus;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/image")
@PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
public class ImageController {

    @Autowired
    ImageService imageService;

    // Upload image
    @PostMapping("/signed")
    public ImageDTO generatev4PutObjectSignedUrl(@RequestBody ImageDTO image) {

        ImageDTO response = imageService.generateV4PutObjectSignedUrl(
                image.getObjectName(), image.getOperatingSystem());

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return response;
    }

    // @PostMapping("/start")
    // public BuildImageDTO startVirtualDiskBuild(@RequestBody ImageDTO image)
    // throws InterruptedException, ExecutionException, IOException {
    //
    // BuildImageDTO response = imageService.startVirtualDiskBuild(
    // image.getObjectName(), image.getImageName());
    //
    // if (response == null) {
    // throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    // }
    //
    // return response;
    // }

    @PostMapping("/cancel")
    public BuildImageDTO cancelVirtualDiskBuild(@RequestBody BuildImageDTO image)
            throws IOException {
        BuildImageDTO response = imageService.cancelVirtualDiskBUild(image.getBuildId());

        return response;
    }

    @GetMapping("/list")
    public List<ImageDTO> listImages() throws IOException {

        return imageService.listImages();
    }

    @PostMapping("/delete")
    public DeleteImageDTO deleteImage(@RequestBody ImageDTO image)
            throws IOException, InterruptedException, ExecutionException {
        String imageName = image.getImageName();

        DeleteImageDTO response = imageService.deleteImage(imageName);

        if (response.getDeleteStatus() == DeleteImageStatus.FAILED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return response;
    }
}
