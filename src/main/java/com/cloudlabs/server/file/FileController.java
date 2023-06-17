package com.cloudlabs.server.file;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.cloudbuild.v1.Build;

@RestController
@RequestMapping("/storage")
public class FileController {

    @Autowired
    FileService fileService;

    //Upload file
    @PostMapping("/signed")
    public ResponseEntity<FileDTO> generatev4PutObjectSignedUrl(@RequestBody FileDTO file) {
        String objectName = file.getObjectName();
        // String fileExtension = FileHelper.getFileExtension(objectName);

        // if (fileExtension == null || (!fileExtension.equalsIgnoreCase("vmdk") && !fileExtension.equalsIgnoreCase("vhd"))) {
        //     throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        // }

        URL signedUploadURL = fileService.generateV4PutObjectSignedUrl(objectName);

        if (signedUploadURL == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        file.setSignedURL(signedUploadURL.toString());
        return ResponseEntity.ok().body(file);
    }

    @PostMapping("/start")
    public FileDTO startVirtualDiskBuild(@RequestBody FileDTO file) throws InterruptedException, ExecutionException, IOException {
        
        Build response = fileService.startVirtualDiskBuild(file.getObjectName(), file.getImageName());

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        file.setBuildId(response.getId());
        file.setBuildStatus(response.getStatus().name());

        return file;
    }
}
