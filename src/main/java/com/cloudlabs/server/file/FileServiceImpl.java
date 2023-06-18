package com.cloudlabs.server.file;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.Credentials;
import com.google.auth.ServiceAccountSigner;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ImpersonatedCredentials;
import com.google.cloud.devtools.cloudbuild.v1.CloudBuildClient;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloudbuild.v1.Build;
import com.google.cloudbuild.v1.BuildOperationMetadata;
import com.google.cloudbuild.v1.BuildStep;
import com.google.cloudbuild.v1.CancelBuildRequest;
import com.google.cloudbuild.v1.CreateBuildRequest;

@Service
public class FileServiceImpl implements FileService {

    @Value("${gcp.bucket.name}")
    private String bucketName;

    @Value("${gcp.project.id}")
    private String projectId;

    @Value("${gcp.signing.service.account}")
    private String serviceAccount;

    /**
     * Signing a URL requires Credentials which implement ServiceAccountSigner. These can be set
     * explicitly using the Storage.SignUrlOption.signWith(ServiceAccountSigner) option. If you don't,
     * you could also pass a service account signer to StorageOptions, i.e.
     * StorageOptions().newBuilder().setCredentials(ServiceAccountSignerCredentials). In this example,
     * neither of these options are used, which means the following code only works when the
     * credentials are defined via the environment variable GOOGLE_APPLICATION_CREDENTIALS, and those
     * credentials are authorized to sign a URL. See the documentation for Storage.signUrl for more
     * details.
     * @throws IOException
     * 
     */
    public URL generateV4PutObjectSignedUrl(String objectName) {
        if (objectName == null) {
            return null;
        }

        String fileExtension = FileHelper.getFileExtension(objectName);

        if (fileExtension == null || (!fileExtension.equalsIgnoreCase("vmdk") && !fileExtension.equalsIgnoreCase("vhd"))) {
            return null;
        }

        Storage storage = StorageOptions.newBuilder()
            .setProjectId(projectId)
            .build()
            .getService();

        // Define Resource
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build();
        
        /** 
         * Get service acccount credentials for signing
         * 
         * https://stackoverflow.com/questions/64811461/gcp-storage-signing-key-not-provided 
         * https://stackoverflow.com/questions/57564505/unable-to-assign-iam-serviceaccounts-signblob-permission
         */
        Credentials credentials = storage.getOptions().getCredentials();

        List<String> scopes = new ArrayList<String>();

        scopes.add("https://www.googleapis.com/auth/iam");
        
        credentials = ImpersonatedCredentials.create(
            (GoogleCredentials) credentials,
            serviceAccount,
            new ArrayList<String>(),
            scopes,
            3600
        );

        // Generate Signed URL
        Map<String, String> extensionHeaders = new HashMap<>();
        extensionHeaders.put("Content-Type", "application/octet-stream");

        URL url =
            storage.signUrl(
                blobInfo,
                15,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withExtHeaders(extensionHeaders),
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.signWith((ServiceAccountSigner) credentials));

        return url;
    }

    /**
     * Check if blob exists on a specific GCP Bucket
     * 
     * @param blobName
     */
    @Override
    public Boolean checkBlobExist(String blobName) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob blob = storage.get(blobId);

        if (blob == null) {
            return false;
        }

        return true;
    }

    @Override
    public Build startVirtualDiskBuild(String objectName, String imageName) throws InterruptedException, ExecutionException, IOException {

        if (objectName == null || imageName == null) {
            return null;
        }

        // Follow GCP image name requirements: 
        // https://cloud.google.com/compute/docs/reference/rest/v1/images
        if (!imageName.matches("[a-z]([-a-z0-9]*[a-z0-9])?")) {
            return null;
        }

        boolean isBlobExist = checkBlobExist(objectName);

        if (!isBlobExist) {
            return null;
        }

        try (CloudBuildClient cloudBuildClient = CloudBuildClient.create()) {
            BuildStep buildStep = BuildStep.newBuilder()
                .addArgs(String.format("-image_name=%s", imageName))
                .addArgs(String.format("-source_file=%s", String.format("gs://%s/%s", bucketName, objectName)))
                .addArgs("-timeout=7000s")
                .addArgs("-client_id=api")
                .setName("gcr.io/compute-image-tools/gce_vm_image_import:release")
                .addEnv("BUILD_ID=$BUILD_ID")
                .build();

            com.google.protobuf.Duration duration = com.google.protobuf.Duration.newBuilder()
                .setSeconds(7200)
                .build();

            Build build = Build.newBuilder()
                .addSteps(0, buildStep)
                .setTimeout(duration)
                .addTags("gce-daisy")
                .addTags("gce-daisy-image-import")
                .build();

            CreateBuildRequest createBuildRequest = CreateBuildRequest.newBuilder()
                .setBuild(build)
                .setProjectId(projectId)
                .build();
            
            OperationFuture<Build, BuildOperationMetadata> operation = cloudBuildClient.createBuildAsync(createBuildRequest);

            // Get the ongoing build without waiting for the whole build to complete,
            // which will take approx. 30 minutes
            Build ongoingBuild = operation.getMetadata().get().getBuild();

            return ongoingBuild;
        }
    }

    @Override
    public Build cancelVirtualDiskBUild(String buildId) throws IOException {

        if (buildId == null) {
            return null;
        }

        try (CloudBuildClient cloudBuildClient = CloudBuildClient.create()) {
            CancelBuildRequest cancelBuildRequest = CancelBuildRequest.newBuilder()
                .setProjectId(projectId)
                .setId(buildId)
                .build();

           Build response = cloudBuildClient.cancelBuild(cancelBuildRequest);

           return response;
        }

    }
    
}
