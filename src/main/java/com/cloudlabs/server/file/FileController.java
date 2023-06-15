package com.cloudlabs.server.file;

import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/storage")
public class FileController {

    @Autowired
    FileService fileService;

    //Upload file
    @PostMapping("/signed")
    public ResponseEntity<String> generatev4PutObjectSignedUrl(@RequestBody JsonNode request) {
        String objectName = request.get("objectName").asText();

        URL signedUploadURL = fileService.generateV4PutObjectSignedUrl(objectName);

        return ResponseEntity.ok(String.format("""
                {
                    \"status\":\"success\",
                    \"signedURL\":\"%s\"
                }
                """, signedUploadURL.toString()));
    }
}
