package com.cloudlabs.server.GCP.compute;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AddAccessConfigInstanceRequest;
import com.google.cloud.compute.v1.Address;
import com.google.cloud.compute.v1.AddressesClient;
import com.google.cloud.compute.v1.InsertAddressRequest;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;

public class ComputeHelper {
	
    public static String reserveStaticExternalIPAddress(String project, String region, String ipAddressName) {
        try (AddressesClient addressClient = AddressesClient.create()) {

            Address addressResource = Address.newBuilder()
                .setName(ipAddressName)
                .build();

            InsertAddressRequest request = InsertAddressRequest.newBuilder()
                .setAddressResource(addressResource)
                .setProject(project)
                .setRegion(region)
                .build();

            Operation response = addressClient.insertAsync(request).get();

            return response.getStatus().toString();
    
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            return e.toString();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return e.getMessage();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return e.getMessage();
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
    public static String getExternalStaticIPAdress(String project, String region, String addressName) {
        try (AddressesClient addressClient = AddressesClient.create()) {
            Address address = addressClient.get(project, region, addressName);
            return address.getAddress();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Address Retrieval Failed";   
        }
    }

    public static String assignStaticExternalIPAddress(String project, String zone, String instanceName, String ipAddress, String networkInterfaceName) throws IOException {
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

            Operation response = instancesClient.addAccessConfigAsync(request).get();

            return response.getStatus().toString();
    
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            return e.toString();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
