package com.cloudlabs.server.compute;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;

import com.cloudlabs.server.compute.dto.AddressDTO;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import com.cloudlabs.server.compute.dto.SourceImageDTO;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AddAccessConfigInstanceRequest;
import com.google.cloud.compute.v1.Address;
import com.google.cloud.compute.v1.AddressesClient;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDisk.Type;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.DeleteInstanceRequest;
import com.google.cloud.compute.v1.InsertAddressRequest;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Items;
import com.google.cloud.compute.v1.Metadata;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ServiceAccount;

@Service
public class ComputeServiceImpl implements ComputeService {
	// https://cloud.google.com/compute/docs/api/libraries
	static String project = "cloudlabs-387310";
	static String zone = "asia-southeast1-b"; 
	static String region = "asia-southeast1";

    @Override
    public ComputeDTO createPublicInstance(ComputeDTO computeInstanceMetadata) {
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
			computeInstanceMetadata.setDiskSizeGb(10);
            computeInstanceMetadata.setNetworkName("default");

			MachineTypeDTO machineTypeDTO = computeInstanceMetadata.getMachineType();
            machineTypeDTO.setZone(zone);
            String machineType = String.format("zones/%s/machineTypes/%s", machineTypeDTO.getZone(), machineTypeDTO.getName());

			SourceImageDTO sourceImageDTO = computeInstanceMetadata.getSourceImage();
			String sourceImage = String
					.format("projects/%s/global/images/family/%s", sourceImageDTO.getProject(), sourceImageDTO.getName());

			long diskSizeGb = computeInstanceMetadata.getDiskSizeGb();
			String networkName = computeInstanceMetadata.getNetworkName();
			String instanceName = computeInstanceMetadata.getInstanceName();
			String startupScript = computeInstanceMetadata.getStartupScript();

            if (startupScript == null) {
                startupScript = "";
            }

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

			// System.out.printf("Creating instance: %s at %s %n", instanceName, zone);

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
				return null;
			}

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
			reserveStaticExternalIPAddress(addressResourceName);

            // Get value of newly created external IP address
            AddressDTO publicIPAddressDTO = getExternalStaticIPAdress(addressResourceName);

            // Attach the Public IP Address to the instance's default network interface: nic0
            assignStaticExternalIPAddress(instanceName, publicIPAddressDTO.getExternalIPv4Address(), "nic0");

            ComputeDTO responseComputeDTO = new ComputeDTO();
            responseComputeDTO.setInstanceName(instanceName);
            responseComputeDTO.setAddressDTO(publicIPAddressDTO);

			return responseComputeDTO;
		} catch (Exception exception) {
            return null;
        }
    }

    @Override
    public ComputeDTO deleteInstance(String instanceName) {
        try (InstancesClient instancesClient = InstancesClient.create()) {

            // Describe which instance is to be deleted.
            DeleteInstanceRequest deleteInstanceRequest = DeleteInstanceRequest.newBuilder()
                .setProject(project)
                .setZone(zone)
                .setInstance(instanceName).build();

            OperationFuture<Operation, Operation> operation = instancesClient.deleteAsync(
                deleteInstanceRequest);
            // Wait for the operation to complete.
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.hasError()) {
                return null;
            }

            ComputeDTO computeDTO = new ComputeDTO();
            computeDTO.setInstanceName(instanceName);
            computeDTO.setStatus(response.getStatus().name());

            return computeDTO;

        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public final void reserveStaticExternalIPAddress(String ipAddressName) 
        throws InterruptedException, ExecutionException, IOException {
        try (AddressesClient addressClient = AddressesClient.create()) {

            Address addressResource = Address.newBuilder()
                .setName(ipAddressName)
                .build();

            InsertAddressRequest request = InsertAddressRequest.newBuilder()
                .setAddressResource(addressResource)
                .setProject(project)
                .setRegion(region)
                .build();

            // Operation response = addressClient.insertAsync(request).get();
            addressClient.insertAsync(request).get();
        }
    }

    /**
     * Get the external static IP address of the instance.
     * 
     * @param project
     * @param region
     * @param addressName
     * @return the external static IP address of the instance
     */
    public AddressDTO getExternalStaticIPAdress(String addressName) {
        try (AddressesClient addressClient = AddressesClient.create()) {
            Address address = addressClient.get(project, region, addressName);

            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setExternalIPv4Address(address.getAddress());

            return addressDTO;
        } catch (Exception exception) {
            return null;   
        }
    }

    public void assignStaticExternalIPAddress(String instanceName, String ipAddress, String networkInterfaceName) 
        throws IOException, InterruptedException, ExecutionException {

        try (InstancesClient instancesClient = InstancesClient.create()) {

            AccessConfig accessConfig = AccessConfig.newBuilder()
                .setName("External NAT")
                .setNatIP(ipAddress)
                .build();

            AddAccessConfigInstanceRequest request = AddAccessConfigInstanceRequest.newBuilder()
                .setAccessConfigResource(accessConfig)
                .setInstance(instanceName)
                .setNetworkInterface(networkInterfaceName) // value should be network interface name (e.g. nic0)
                .setProject(project)
                .setZone(zone)
                .build();

            // Operation response = instancesClient.addAccessConfigAsync(request).get();
            instancesClient.addAccessConfigAsync(request).get();
        }
    }
}