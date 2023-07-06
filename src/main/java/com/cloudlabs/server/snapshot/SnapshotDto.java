package com.cloudlabs.server.snapshot;

public class SnapshotDto {
    // TODO(developer): Replace these variables before running the sample.
    // You need to pass `zone` or `region` parameter relevant to the disk you want to
    // snapshot, but not both. Pass `zone` parameter for zonal disks and `region` for
    // regional disks.

    // Project ID or project number of the Cloud project you want to use.
    private final String projectId = "cloudlabs-387310";

    // Name of the disk you want to create.
    private String diskName;

    // Name of the snapshot that you want to create.
    private String snapshotName;

    // The zone of the source disk from which you create the snapshot (for zonal disks).
    private String zone;

    // The region of the source disk from which you create the snapshot (for regional disks).
    private String region; //asia-southeast1

    // The Cloud Storage multi-region or the Cloud Storage region where you
    // want to store your snapshot.
    // You can specify only one storage location. Available locations:
    // https://cloud.google.com/storage/docs/locations#available-locations
    private String location;

    // Project ID or project number of the Cloud project that
    // hosts the disk you want to snapshot. If not provided, the value will be defaulted
    // to 'projectId' value.
    private String diskProjectId;

    public String getProjectId() {
        return projectId;
    }

    public String getDiskName() {
        return diskName;
    }

    public void setDiskName(String diskName) {
        this.diskName = diskName;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDiskProjectId() {
        return diskProjectId;
    }

    public void setDiskProjectId(String diskProjectId) {
        this.diskProjectId = diskProjectId;
    }
}

