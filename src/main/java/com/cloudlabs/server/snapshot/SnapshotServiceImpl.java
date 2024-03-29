package com.cloudlabs.server.snapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cloudlabs.server.compute.Compute;
import com.cloudlabs.server.compute.ComputeRepository;
import com.cloudlabs.server.compute.ComputeService;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.snapshot.dto.SaveSnapshotDTO;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDisk.Type;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;


@Service
public class SnapshotServiceImpl implements SnapshotService {
    
    // Project ID or project number of the Cloud project you want to use.
    @Value("${gcp.project.id}")
    private String projectId;

    // The zone of the source disk from which you create the snapshot (for zonal disks).
    @Value("${gcp.project.zone}")
    private String zone; //asia-southeast1-b

    // The region of the source disk from which you create the snapshot (for regional disks).
    private String region = ""; //asia-southeast1

    // The Cloud Storage multi-region or the Cloud Storage region where you
    // want to store your snapshot.
    // You can specify only one storage location. Available locations:
    // https://cloud.google.com/storage/docs/locations#available-locations
    @Value("${gcp.project.region}")
    private String location;

    // Project ID or project number of the Cloud project that
    // hosts the disk you want to snapshot. If not provided, the value will be defaulted
    // to 'projectId' value.
    @Value("${gcp.disk.project}")
    private String diskProjectId;

    @Autowired
    private ComputeService computeService;

    @Autowired
    private SnapshotRepository snapshotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComputeRepository computeRepository;

    @Override
    public SaveSnapshotDTO createSnapshot(SaveSnapshotDTO saveSnapshotDTO)
        throws IOException, ExecutionException, InterruptedException, TimeoutException {

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `snapshotsClient.close()` method on the client to safely
        // clean up any remaining background resources.
        
        // Get current user from security context
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authenticationToken.getName();

        String snapshotName = saveSnapshotDTO.getSnapshotName();
        String diskName = saveSnapshotDTO.getInstanceName();
        String description = saveSnapshotDTO.getDescription();

        // get compute instance
        Compute compute = computeRepository.findByUsers_EmailAndInstanceName(email, saveSnapshotDTO.getInstanceName())
            .orElse(null);

        if (compute == null) {
            return null;
        }

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
            disk = regionDisksClient.get(projectId, region, diskName);
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
            .get(5, TimeUnit.MINUTES);

        if (operation.hasError()) {
            System.out.println("Snapshot creation failed!" + operation);
            return null;
        }

        // Retrieve the created snapshot.
        Snapshot snapshot = snapshotsClient.get(projectId, snapshotName);
        System.out.println(String.format("Snapshot created: %s", snapshot.getName()));

        // Find user by email
        User user = userRepository.findByEmail(email).get();
        System.out.println(user.getFullname());
        SaveSnapshot saveSnapshot = new SaveSnapshot(snapshotName, description, user, compute.getInstanceName());
        snapshotRepository.save(saveSnapshot);

        SaveSnapshotDTO returnDTO = new SaveSnapshotDTO(snapshotName, description, diskName); // diskName = instanceName
        return returnDTO;

        }
    }

    // Delete a snapshot of a disk.
    @Override
    public SaveSnapshotDTO deleteSnapshot(SaveSnapshotDTO saveSnapshotDTO)
        throws IOException, ExecutionException, InterruptedException, TimeoutException {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests. After completing all of your requests, call
            // the `snapshotsClient.close()` method on the client to safely
            // clean up any remaining background resources.
            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext()
                .getAuthentication();

            String email = authenticationToken.getName();
            String snapshotName = saveSnapshotDTO.getSnapshotName();
            String computeInstance = saveSnapshotDTO.getInstanceName();

            Compute compute = computeRepository.findByUsers_EmailAndInstanceName(email, computeInstance)
                .orElse(null);

            SaveSnapshot saveSnapshot = snapshotRepository.findBySnapshotNameAndInstanceName(snapshotName, compute.getInstanceName())
                .orElse(null);
            
            if (saveSnapshot == null) {
                System.out.println("no snapshot");
                return null;
            }

            try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {

            Operation operation = snapshotsClient.deleteAsync(projectId, snapshotName)
                .get(3, TimeUnit.MINUTES);

            if (operation.hasError()) {
                System.out.println("Snapshot deletion failed!" + operation);
                return null;
            }

            snapshotRepository.delete(saveSnapshot);

            System.out.println("Snapshot deleted!");
            }

            SaveSnapshotDTO returnDTO = new SaveSnapshotDTO();
            returnDTO.setSnapshotName(snapshotName);
            returnDTO.setInstanceName(computeInstance);

            return returnDTO;
    }

    @Override
    public List<SaveSnapshotDTO> listSnapshots(SaveSnapshotDTO saveSnapshotDTO) throws IOException { //String snapshotName
        
        // Get email from Jwt token using context
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authenticationToken.getName();
        System.out.println(email);
        System.out.println(saveSnapshotDTO.getInstanceName());

        Compute compute = computeRepository.findByUsers_EmailAndInstanceName(email, saveSnapshotDTO.getInstanceName())
            .orElse(null);

        if (compute == null) {
            System.out.println("no instance");
            return null;
        }

        List<SaveSnapshot> snapshotList = snapshotRepository.findByUser_EmailAndInstanceName(email, compute.getInstanceName());

        List<SaveSnapshotDTO> saveSnapshotDtos = new ArrayList<>();

        for (SaveSnapshot i : snapshotList) {
            SaveSnapshotDTO saveSnapshotDto = new SaveSnapshotDTO();
            saveSnapshotDto.setSnapshotName(i.getSnapshotName());
            saveSnapshotDto.setDescription(i.getDescription());
            saveSnapshotDto.setInstanceName(i.getInstanceName());
            saveSnapshotDtos.add(saveSnapshotDto);
        }

        return saveSnapshotDtos;
        
    }

    // Get information about a snapshot.
    /*
    @Override
    public void getSnapshot(String snapshotName) throws IOException {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `snapshotsClient.close()` method on the client to safely
        // clean up any remaining background resources.
        try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {
            Snapshot snapshot = snapshotsClient.get(projectId, snapshotName);
            System.out.printf("Retrieved the snapshot: %s", snapshot.getName());
        }
    }
    */

    // Attaches snapshot to instance's disk
    /**
   * @param diskType the type of disk you want to create. This value uses the following format:
   * "zones/{zone}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)". For example:
   * "zones/us-west3-b/diskTypes/pd-ssd"
   * @param diskSizeGb size of the new disk in gigabytes
   * @param boot boolean flag indicating whether this disk should be used as a boot disk of an
   * instance
   * @param diskSnapshot disk snapshot to use when creating this disk. You must have read access to
   * this disk. This value uses the following format:
   * "projects/{project_name}/global/snapshots/{snapshot_name}"
   * @return AttachedDisk object configured to be created using the specified snapshot.
   */
    public AttachedDisk diskFromSnapshot(
        String diskType, long diskSizeGb,
        boolean boot, String diskSnapshot) {
    AttachedDisk disk =
        AttachedDisk.newBuilder()
            .setBoot(boot)
            // Remember to set auto_delete to True if you want the disk to be deleted when
            // you delete your VM instance.
            .setAutoDelete(true)
            .setType(Type.PERSISTENT.toString())
            .setInitializeParams(
                AttachedDiskInitializeParams.newBuilder()
                    .setSourceSnapshot(String.format("projects/%s/global/snapshots/%s",projectId,diskSnapshot))
                    .setDiskSizeGb(diskSizeGb)
                    .setDiskType(String.format("zones/%s/diskTypes/pd-standard", zone))
                    .build())
            .build();
    return disk;
    }

    /**
   * Create a new VM instance with boot disk created from a snapshot.
   *
   * @param project project ID or project number of the Cloud project you want to use.
   * @param zone name of the zone to create the instance in. For example: "us-west3-b"
   * @param instanceName name of the new virtual machine (VM) instance.
   * @param snapshotName link to the snapshot you want to use as the source of your boot disk in the
   * form of: "projects/{project_name}/global/snapshots/{snapshot_name}"
   * @return Instance object.
   */
    public ComputeDTO createFromSnapshot(SaveSnapshotDTO saveSnapshotDTO)
        throws IOException, InterruptedException, ExecutionException, TimeoutException {
            // Get email from Jwt token using context
            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            String email = authenticationToken.getName();
            String instanceName = saveSnapshotDTO.getInstanceName();
            String snapshotName = saveSnapshotDTO.getSnapshotName();

            Compute compute = computeRepository.findByUsers_EmailAndInstanceName(email, instanceName).get();

            SaveSnapshot saveSnapshot = snapshotRepository.findBySnapshotNameAndInstanceName(snapshotName, compute.getInstanceName())
                .orElse(null);
                if (saveSnapshot == null) {
                    return null;
                }
            // Get computeDTO
            ComputeDTO computeDTO = computeService.getComputeInstance(instanceName);
            
            System.out.println(computeDTO);
            if (computeDTO == null){
                return null;
            }
            // then delete previous instance
            computeService.deleteInstance(instanceName);

            // create and attach new disk
            String diskType = String.format("zones/%s/diskTypes/pd-standard", zone);
            AttachedDisk disk = diskFromSnapshot(diskType, computeDTO.getDiskSizeGb(), true, snapshotName);
            //createWithDisks(instanceName, disks, "e2-medium", "default", null);
            return computeService.createPrivateInstance(computeDTO, disk);
    }

}
