package com.cloudlabs.server.snapshot;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.ListSnapshotsRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;


@Service
public class SnapshotServiceImpl implements SnapshotService {
    
    // Project ID or project number of the Cloud project you want to use.
    private final String projectId = "cloudlabs-387310";

    // Name of the disk you want to create.
    private String diskName;

    // Name of the snapshot that you want to create.
    private String snapshotName;

    // The zone of the source disk from which you create the snapshot (for zonal disks).
    private String zone = "asia-southeast1-b";

    // The region of the source disk from which you create the snapshot (for regional disks).
    private String region = ""; //asia-southeast1

    // The Cloud Storage multi-region or the Cloud Storage region where you
    // want to store your snapshot.
    // You can specify only one storage location. Available locations:
    // https://cloud.google.com/storage/docs/locations#available-locations
    private String location = "asia-southeast1";

    // Project ID or project number of the Cloud project that
    // hosts the disk you want to snapshot. If not provided, the value will be defaulted
    // to 'projectId' value.
    private String diskProjectId = "snapshot-storage-1";

    public SnapshotServiceImpl() {}

    @Override
    public void createSnapshot(String snapshotName, String diskName
    /*     
    String projectId, String diskName, String snapshotName,
        String zone, String region, String location, String diskProjectId
        */
        )
        throws IOException, ExecutionException, InterruptedException, TimeoutException {

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `snapshotsClient.close()` method on the client to safely
        // clean up any remaining background resources.
        try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {

        if (zone.isEmpty() && region.isEmpty()) {
            throw new Error("You need to specify 'zone' or 'region' for this function to work");
        }

        if (!zone.isEmpty() && !region.isEmpty()) {
            throw new Error("You can't set both 'zone' and 'region' parameters");
        }

        // If Disk's project id is not specified, then the projectId parameter will be used.
        if (diskProjectId.isEmpty()) {
            diskProjectId = projectId;
        }

        // If zone is not empty, use the DisksClient to create a disk.
        // Else, use the RegionDisksClient.
        Disk disk;
        if (!zone.isEmpty()) {
            DisksClient disksClient = DisksClient.create();
            disk = disksClient.get(projectId, zone, diskName);
        } else {
            RegionDisksClient regionDisksClient = RegionDisksClient.create();
            disk = regionDisksClient.get(projectId, zone, diskName);
        }

        // Set the snapshot properties.
        Snapshot snapshotResource;
        if (!location.isEmpty()) {
            snapshotResource = Snapshot.newBuilder()
                .setName(snapshotName)
                .setSourceDisk(disk.getSelfLink())
                .addStorageLocations(location)
                .build();
        } else {
            snapshotResource = Snapshot.newBuilder()
                .setName(snapshotName)
                .setSourceDisk(disk.getSelfLink())
                .build();
        }

        // Wait for the operation to complete.
        Operation operation = snapshotsClient.insertAsync(projectId, snapshotResource)
            .get(3, TimeUnit.MINUTES);

        if (operation.hasError()) {
            System.out.println("Snapshot creation failed!" + operation);
            return;
        }

        // Retrieve the created snapshot.
        Snapshot snapshot = snapshotsClient.get(projectId, snapshotName);
        System.out.printf("Snapshot created: %s", snapshot.getName());

        }
    }

    // Delete a snapshot of a disk.
    @Override
    public void deleteSnapshot(//String projectId, 
    String snapshotName)
        throws IOException, ExecutionException, InterruptedException, TimeoutException {

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `snapshotsClient.close()` method on the client to safely
        // clean up any remaining background resources.
        try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {

        Operation operation = snapshotsClient.deleteAsync(projectId, snapshotName)
            .get(3, TimeUnit.MINUTES);

        if (operation.hasError()) {
            System.out.println("Snapshot deletion failed!" + operation);
            return;
        }

        System.out.println("Snapshot deleted!");
        }
    }

    @Override
    public void listSnapshots(String filter) throws IOException { //String projectId, 

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `snapshotsClient.close()` method on the client to safely
        // clean up any remaining background resources.
        try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {

        // Create the List Snapshot request.
            ListSnapshotsRequest listSnapshotsRequest = ListSnapshotsRequest.newBuilder()
                .setProject(projectId)
                .setFilter(filter)
                .build();

            System.out.println("List of snapshots:");
            for (Snapshot snapshot : snapshotsClient.list(listSnapshotsRequest).iterateAll()) {
                System.out.println(snapshot.getName());
            }
        }
    }

    // Get information about a snapshot.
    @Override
    public void getSnapshot(String snapshotName) throws IOException { //String projectId, 
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `snapshotsClient.close()` method on the client to safely
        // clean up any remaining background resources.
        try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {
            Snapshot snapshot = snapshotsClient.get(projectId, snapshotName);
            System.out.printf("Retrieved the snapshot: %s", snapshot.getName());
        }
    }
}
