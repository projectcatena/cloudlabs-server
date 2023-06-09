package com.cloudlabs.server.GCP.compute;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Items;
import com.google.cloud.compute.v1.Metadata;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ServiceAccount;
import com.google.cloud.compute.v1.AttachedDisk.Type;

@RestController
@RequestMapping("/compute")
public class ComputeEngine {
	// https://cloud.google.com/compute/docs/api/libraries
	static String project = "cloudlabs-387310";
	static String zone = "asia-southeast1-b"; 
	static String region = "asia-southeast1";

	// Create a new public instance with the provided "instanceName" value in the specified
	// project and zone.
	@PostMapping("/create")
	public static String createPublicInstance(@RequestBody JsonNode request)
			throws IOException, InterruptedException, ExecutionException, TimeoutException {

		// Initialize client that will be used to send requests. This client only needs
		// to be created
		// once, and can be reused for multiple requests. After completing all of your
		// requests, call
		// the `instancesClient.close()` method on the client to safely
		// clean up any remaining background resources.
		try (InstancesClient instancesClient = InstancesClient.create()) {
			// Below are sample values that can be replaced.
			// machineType: machine type of the VM being created.
			// * This value uses the format zones/{zone}/machineTypes/{type_name}.
			// * For a list of machine types, see
			// https://cloud.google.com/compute/docs/machine-types
			// sourceImage: path to the operating system image to mount.
			// * For details about images you can mount, see
			// https://cloud.google.com/compute/docs/images
			// diskSizeGb: storage size of the boot disk to attach to the instance.
			// networkName: network interface to associate with the instance.
			String machineType = String.format("zones/asia-southeast1-b/machineTypes/%s",
					request.get("selectedInstanceType").get("name").asText());
			String sourceImage = String
					.format("%s%s", request.get("selectedImage").get("project").asText(),
							request.get("selectedImage").get("name").asText());
			long diskSizeGb = 10L;
			String networkName = "default";
			String instanceName = request.get("name").asText();
			String startupScript = request.get("script").asText();

			// Instance creation requires at least one persistent disk and one network
			// interface.
			AttachedDisk disk = AttachedDisk.newBuilder()
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

            ServiceAccount serviceAccount = ServiceAccount.newBuilder()
                    .setEmail("default")
                    .addScopes("https://www.googleapis.com/auth/devstorage.read_only")
                    .build();

			// Startup script for the instance
			Items items = Items.newBuilder()
					.setKey("startup-script")
					.setValue(startupScript) // Authenticated or gsutil URL
					.build();

			Metadata metadata = Metadata.newBuilder()
					.addItems(items)
					.build();

			// Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to an
			// instance.
			Instance instanceResource = Instance.newBuilder()
					.setName(instanceName)
					.setMachineType(machineType)
					.setMetadata(metadata)
					.addServiceAccounts(serviceAccount)
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
				return "{ \"status\": \"error\" }";
			}

			System.out.println("Operation Status: " + response.getStatus());

            /*
             * Allow the instance to be publicly accessible, only if the instance creation is successful.
             * 
             * Steps:
             * 1. Reserve a static external IP address
             * 2. Get value of newly created external IP address
             * 2. Assign the static external IP address to the instance
             */
            // Reserve Public IP Address for the instance
            String addressResourceName = String.format("%s-public-ip", instanceName);
			String publicIPAddressResposne = AddressHelper.reserveStaticExternalIPAddress(project, region, addressResourceName);
			System.out.println(publicIPAddressResposne);

            // Get value of newly created external IP address
            String publicIPAddress = AddressHelper.getExternalStaticIPAdress(project, region, addressResourceName);
            System.out.println(publicIPAddress);

            // Attach the Public IP Address to the instance's default network interface: nic0
            String attachPublicIPAddressResponse = AddressHelper.assignStaticExternalIPAddress(project, zone, instanceName, publicIPAddress, "nic0");
            System.out.println(attachPublicIPAddressResponse);

			return "{ \"status\": \"success\" }";
		} catch (IllegalArgumentException illegalArgumentException) {
			// Should implement custom exception handler, as "server.error.include-message=always" 
			// workaround may disclose sensitive internal exceptions
			// Source: https://stackoverflow.com/questions/62561211/spring-responsestatusexception-does-not-return-reason
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal parameters.");
		}
	}
}