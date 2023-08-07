package com.cloudlabs.server.snapshot;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.snapshot.dto.SaveSnapshotDTO;

@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RequestMapping("snapshot")
@RestController()
public class SnapshotController {
    
    private SnapshotService snapshotService;

    //private SnapshotDto snapshotDto;

    public SnapshotController(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    @PostMapping(path = "create")
    @PreAuthorize("hasAnyRole('USER','TUTOR','ADMIN')")
    public SaveSnapshotDTO createSnapshot(
        @RequestBody SaveSnapshotDTO saveSnapshotDTO
    ) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        SaveSnapshotDTO result = snapshotService.createSnapshot(saveSnapshotDTO.getSnapshotName(),
        saveSnapshotDTO.getInstanceName(),
        saveSnapshotDTO.getDescription());
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        return result;
    }

    @DeleteMapping(path = "delete")
    @PreAuthorize("hasAnyRole('USER','TUTOR','ADMIN')")
    public String deleteSnapshot(
        @RequestBody SaveSnapshotDTO saveSnapshotDTO) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        SaveSnapshotDTO result = snapshotService.deleteSnapshot(saveSnapshotDTO.getSnapshotName());
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return "snapshot deleted";
    }

    @GetMapping(path = "list")
    @PreAuthorize("hasAnyRole('USER','TUTOR','ADMIN')")
    public List<SaveSnapshotDTO> listSnapshots() throws IOException {
        return snapshotService.listSnapshots();
    }

    /*
    @GetMapping(path = "get")
    @PreAuthorize("hasAnyAuthority('USER','TUTOR','ADMIN')")
    public String getSnapshotList(
        @RequestParam String snapshotName) throws IOException { //SnapshotDto
        //var projectId = snapshotDto.getProjectId();
        snapshotService.getSnapshot(snapshotName); //projectId, 
        return "get snapshot";
    }
    */

    @PostMapping(path = "revert")
    @PreAuthorize("hasAnyRole('USER','TUTOR','ADMIN')")
    public ComputeDTO revertToSnapshot(
        @RequestBody SaveSnapshotDTO  saveSnapshotDTO
    ) throws InterruptedException, ExecutionException, TimeoutException,IOException {
        
        ComputeDTO result = snapshotService.createFromSnapshot(saveSnapshotDTO.getInstanceName(),
        saveSnapshotDTO.getSnapshotName());
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return result;
    }

}

