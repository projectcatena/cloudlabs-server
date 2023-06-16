package com.cloudlabs.server.file;

import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

        // return ResponseEntity.ok(String.format("""
        //         {
        //             \"status\":\"success\",
        //             \"signedURL\":\"%s\"
        //         }
        //         """, signedUploadURL.toString()));
        file.setSignedURL(signedUploadURL.toString());
        return ResponseEntity.ok().body(file);
    }
}
