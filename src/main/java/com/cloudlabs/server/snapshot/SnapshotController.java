package com.cloudlabs.server.snapshot;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudlabs.server.compute.dto.ComputeDTO;

@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RequestMapping("snapshot")
@RestController()
public class SnapshotController {
    
    private SnapshotService snapshotService;

    //private SnapshotDto snapshotDto;

    public SnapshotController(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    @PostMapping(path = "create", consumes = (MediaType.APPLICATION_FORM_URLENCODED_VALUE))
    @PreAuthorize("hasAnyAuthority('USER','TUTOR','ADMIN')")
    public String createSnapshot(
        @RequestParam String snapshotName,
        @RequestParam String diskName,
        @RequestParam String description
    ) throws IOException, ExecutionException, InterruptedException, TimeoutException { //, @RequestParam String projectId, @RequestParam String diskName, @RequestParam String zone, @RequestParam String region, @RequestParam String location, @RequestParam String diskProjectId
        snapshotService.createSnapshot(snapshotName, diskName, description); //change param
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
    public List<SaveSnapshot> listSnapshots(
        //@RequestParam String filter //SnapshotDto
    ) throws IOException {
        //var projectId = snapshotDto.getProjectId();
        //List<SaveSnapshot> snapshotList =  //projectId, 
        return snapshotService.listSnapshots();
    }

    @GetMapping(path = "get")
    @PreAuthorize("hasAnyAuthority('USER','TUTOR','ADMIN')")
    public String getSnapshotList(
        @RequestParam String snapshotName) throws IOException { //SnapshotDto
        //var projectId = snapshotDto.getProjectId();
        snapshotService.getSnapshot(snapshotName); //projectId, 
        return "get snapshot";
    }

    @PostMapping(path = "revert", consumes = (MediaType.APPLICATION_FORM_URLENCODED_VALUE))
    @PreAuthorize("hasAnyAuthority('USER','TUTOR','ADMIN')")
    public String revertToSnapshot(
        @RequestParam String instanceName,
        @RequestParam String snapshotName
        //@RequestParam String snapshotName
    ) throws InterruptedException, ExecutionException, TimeoutException,IOException {
        
        ComputeDTO result = snapshotService.createFromSnapshot(instanceName, snapshotName);
        if (result != null) {
            return "revert snapshot";
        }
        return "Unable to revert to snapshot";
    }

}

