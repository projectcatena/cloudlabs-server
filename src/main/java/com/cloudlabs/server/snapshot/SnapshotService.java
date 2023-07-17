package com.cloudlabs.server.snapshot;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.snapshot.dto.SaveSnapshotDTO;
import com.google.cloud.compute.v1.AttachedDisk;

/**
 * SnapshotService
 */
public interface SnapshotService {

    void createSnapshot(String snapshotName, String diskName, String description) //String projectId, String diskName, String snapshotName,String zone, String region, String location, String diskProjectId
        throws IOException, ExecutionException, InterruptedException, TimeoutException;
    
    void deleteSnapshot(String snapshotName) //String projectId, 
        throws IOException, ExecutionException, InterruptedException, TimeoutException;

    List<SaveSnapshotDTO> listSnapshots() throws IOException; //String projectId, 
    
    //void getSnapshot(String snapshotName) throws IOException; //String projectId, 

    ComputeDTO createFromSnapshot(
        String instanceName, String snapshotName)
        throws IOException, InterruptedException, ExecutionException, TimeoutException;

    AttachedDisk diskFromSnapshot(String diskType, long diskSizeGb, boolean boot, String diskSnapshot);
}
