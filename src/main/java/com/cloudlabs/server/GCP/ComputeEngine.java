package com.cloudlabs.server.GCP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.ListImagesRequest;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.AttachedDisk.Type;
import com.google.gson.Gson;

@RestController
@RequestMapping("/compute")
public class ComputeEngine {
    // https://cloud.google.com/compute/docs/api/libraries
    static String project = "cloudlabs-387310";
    static String zone = "asia-southeast1-b"; // Region: asia-southeast1 (Singapore)

    @GetMapping("/list")
    public static String listInstances() throws IOException {
        Gson gson = new Gson();
        ArrayList<Instance> instances = new ArrayList<Instance>();
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `instancesClient.close()` method on the client to 
        // safely clean up any remaining background resources.
        try (InstancesClient instancesClient = InstancesClient.create()) {
            // Set the project and zone to retrieve instances present in the zone.
            // System.out.printf("Listing instances from %s in %s:", project, zone);
            for (Instance zoneInstance : instancesClient.list(project, zone).iterateAll()) {
                instances.add(zoneInstance);
            }

            String json = gson.toJson(instances);

            return json;
        } catch (Error error) {
            return error.toString();
        }
    }

    // Prints a list of all non-deprecated image names available in given project.
    @GetMapping("/list-images")
    public static String listImages() throws IOException {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `instancesClient.close()` method on the client to
        // safely clean up any remaining background resources.
        try (ImagesClient imagesClient = ImagesClient.create()) {

            // Listing only non-deprecated images to reduce the size of the reply. (Does not list marketplace images)
            // To list marketplace images, must find their project (e.g. ubuntu-os-cloud)
            ListImagesRequest imagesRequest = ListImagesRequest.newBuilder()
                .setProject(project)
                .setMaxResults(100)
                .setFilter("deprecated.state != DEPRECATED")
                .build();
            
            List<Image> images = new ArrayList<Image>();

            // Although the `setMaxResults` parameter is specified in the request, the iterable returned
            // by the `list()` method hides the pagination mechanic. The library makes multiple
            // requests to the API for you, so you can simply iterate over all the images.
            int imageCount = 0;
            for (Image image : imagesClient.list(imagesRequest).iterateAll()) {
                imageCount++;
                images.add(image);
                System.out.println(image.getName());
            }
            System.out.printf("Image count in %s is: %s", project, imageCount);
            
            return new Gson().toJson(images); 
        }
    }

    // Create a new instance with the provided "instanceName" value in the specified project and zone.
    @PostMapping("/create")
    public static String createInstance(@RequestBody JsonNode payload)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
        // Below are sample values that can be replaced.
        // machineType: machine type of the VM being created.
        // *   This value uses the format zones/{zone}/machineTypes/{type_name}.
        // *   For a list of machine types, see https://cloud.google.com/compute/docs/machine-types
        // sourceImage: path to the operating system image to mount.
        // *   For details about images you can mount, see https://cloud.google.com/compute/docs/images
        // diskSizeGb: storage size of the boot disk to attach to the instance.
        // networkName: network interface to associate with the instance.
        String machineType = String.format("zones/%s/machineTypes/e2-micro", zone);
        String sourceImage = String
            .format("projects/debian-cloud/global/images/family/%s", payload.get("image").asText());
        long diskSizeGb = 10L;
        String networkName = "default";
        String instanceName = payload.get("name").asText();

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `instancesClient.close()` method on the client to safely
        // clean up any remaining background resources.
        try (InstancesClient instancesClient = InstancesClient.create()) {
            // Instance creation requires at least one persistent disk and one network interface.
            AttachedDisk disk =
                AttachedDisk.newBuilder()
                    .setBoot(true)
                    .setAutoDelete(true)
                    .setType(Type.PERSISTENT.toString())
                    .setDeviceName("disk-1")
                    .setInitializeParams(
                        AttachedDiskInitializeParams.newBuilder()
                            .setSourceImage(sourceImage)
                            .setDiskSizeGb(diskSizeGb)
                            .build())
                    .build();

            // Use the network interface provided in the networkName argument.
            NetworkInterface networkInterface = NetworkInterface.newBuilder()
                .setName(networkName)
                .build();

            // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to an instance.
            Instance instanceResource =
                Instance.newBuilder()
                    .setName(instanceName)
                    .setMachineType(machineType)
                    .addDisks(disk)
                    .addNetworkInterfaces(networkInterface)
                    .build();

            System.out.printf("Creating instance: %s at %s %n", instanceName, zone);

            // Insert the instance in the specified project and zone.
            InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
                .setProject(project)
                .setZone(zone)
                .setInstanceResource(instanceResource)
                .build();

            OperationFuture<Operation, Operation> operation = instancesClient.insertAsync(
                insertInstanceRequest);

            // Wait for the operation to complete.
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.hasError()) {
                System.out.println("Instance creation failed ! ! " + response);
                return "error";
            }
            System.out.println("Operation Status: " + response.getStatus());

            return "success";
        }
    }
}