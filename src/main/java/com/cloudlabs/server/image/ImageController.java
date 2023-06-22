package com.cloudlabs.server.image;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.cloudbuild.v1.Build;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    ImageService imageService;

    //Upload image
    @PostMapping("/signed")
    public ResponseEntity<ImageDTO> generatev4PutObjectSignedUrl(@RequestBody ImageDTO image) {
        String objectName = image.getObjectName();

        URL signedUploadURL = imageService.generateV4PutObjectSignedUrl(objectName);

        if (signedUploadURL == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        image.setSignedURL(signedUploadURL.toString());
        return ResponseEntity.ok().body(image);
    }

    @PostMapping("/start")
    public ImageDTO startVirtualDiskBuild(@RequestBody ImageDTO image) throws InterruptedException, ExecutionException, IOException {
        
        Build response = imageService.startVirtualDiskBuild(image.getObjectName(), image.getImageName());

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        image.setBuildId(response.getId());
        image.setBuildStatus(response.getStatus().name());

        return image;
    }

    @PostMapping("/cancel")
    public ImageDTO cancelVirtualDiskBuild(@RequestBody ImageDTO image) throws IOException {
        Build response = imageService.cancelVirtualDiskBUild(image.getBuildId());

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        image.setBuildStatus(response.getStatus().name());

        return image;
    }

    @GetMapping("/list")
    public List<ImageDTO> listImages() throws IOException {

        return imageService.listImages();
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestBody ImageDTO image) throws IOException, InterruptedException, ExecutionException {
        String imageName = image.getImageName();

        try {
            imageService.deleteImage(imageName);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    
        return ResponseEntity.ok().build();
    }
}
