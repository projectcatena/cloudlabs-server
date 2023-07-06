package com.cloudlabs.server.snapshot;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RequestMapping("snapshot")
@RestController()
public class SnapshotController {
    
    private SnapshotService snapshotService;

    //private SnapshotDto snapshotDto;

    public SnapshotController(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
        //this.snapshotDto = snapshotDto;
    }

    @PutMapping(path = "create", consumes = (MediaType.APPLICATION_FORM_URLENCODED_VALUE))
    @PreAuthorize("hasAnyAuthority('USER','TUTOR','ADMIN')")
    public String createSnapshot(
        @RequestParam String snapshotName,
        @RequestParam String diskName
    ) throws IOException, ExecutionException, InterruptedException, TimeoutException { //, @RequestParam String projectId, @RequestParam String diskName, @RequestParam String zone, @RequestParam String region, @RequestParam String location, @RequestParam String diskProjectId
        snapshotService.createSnapshot(snapshotName, diskName); //change param
        return "create snapshot";
    }

    @DeleteMapping(path = "delete", consumes = (MediaType.APPLICATION_FORM_URLENCODED_VALUE))
    @PreAuthorize("hasAnyAuthority('USER','TUTOR','ADMIN')")
    public String deleteSnapshot(
        @RequestParam String snapshotName) throws IOException, ExecutionException, InterruptedException, TimeoutException { //SnapshotDto
        //var projectId = snapshotDto.getProjectId();
        snapshotService.deleteSnapshot(snapshotName); //change param projectId, 
        return "snapshot deleted";
    }

    @GetMapping(path = "list")
    @PreAuthorize("hasAnyAuthority('USER','TUTOR','ADMIN')")
    public String listSnapshots(
        @RequestParam String filter //SnapshotDto
    ) throws IOException {
        //var projectId = snapshotDto.getProjectId();
        snapshotService.listSnapshots(filter); //projectId, 
        return "list snapshots";
    }

    @GetMapping(path = "get")
    @PreAuthorize("hasAnyAuthority('USER','TUTOR','ADMIN')")
    public String revertToSnapshot(
        @RequestParam String snapshotName) throws IOException { //SnapshotDto
        //var projectId = snapshotDto.getProjectId();
        snapshotService.getSnapshot(snapshotName); //projectId, 
        return "get snapshot";
    }

}

