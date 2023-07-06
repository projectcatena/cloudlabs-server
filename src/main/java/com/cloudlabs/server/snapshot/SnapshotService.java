package com.cloudlabs.server.snapshot;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * SnapshotService
 */
public interface SnapshotService {

    void createSnapshot(String snapshotName, String diskName) //String projectId, String diskName, String snapshotName,String zone, String region, String location, String diskProjectId
        throws IOException, ExecutionException, InterruptedException, TimeoutException;
    
    void deleteSnapshot(String snapshotName) //String projectId, 
        throws IOException, ExecutionException, InterruptedException, TimeoutException;

    void listSnapshots(String filter) throws IOException; //String projectId, 
    
    void getSnapshot(String snapshotName) throws IOException; //String projectId, 
}
