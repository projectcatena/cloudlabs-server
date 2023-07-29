package com.cloudlabs.server.subnet;

import com.cloudlabs.server.subnet.dto.SubnetDTO;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.DeleteSubnetworkRequest;
import com.google.cloud.compute.v1.InsertSubnetworkRequest;
import com.google.cloud.compute.v1.ListSubnetworksRequest;
import com.google.cloud.compute.v1.Subnetwork;
import com.google.cloud.compute.v1.SubnetworksClient;
import com.google.cloud.compute.v1.SubnetworksSettings;
import com.google.cloud.compute.v1.Network;
import com.google.cloud.compute.v1.NetworksClient;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubnetServiceImpl implements SubnetService {
    static String project = "cloudlabs-387310";
    static String region = "asia-southeast1";
    static String networkName = "cloudlabs-staging";

    @Autowired SubnetRepository subnetRepository;

    @Override
    public SubnetDTO createSubnet(SubnetDTO subnetDTO) {

        try (SubnetworksClient subnetworksClient = SubnetworksClient.create()) {

            NetworksClient networksClient = NetworksClient.create();
            Network network = networksClient.get(project, networkName);

            String subnetName = subnetDTO.getSubnetName();
            String ipv4Range = subnetDTO.getIpv4Range();

            Subnetwork subnetworkResources = Subnetwork.newBuilder()
                    .setNetwork(network.getSelfLink())
                    .setName(subnetName)
                    .setRegion(region)
                    .setIpCidrRange(ipv4Range)
                    .build();

            InsertSubnetworkRequest insertSubnetworkRequest = InsertSubnetworkRequest.newBuilder()
                    .setProject(project)
                    .setRegion(region)
                    .setSubnetworkResource(subnetworkResources)
                    .build();

            OperationFuture<Operation, Operation> operation = subnetworksClient.insertAsync(insertSubnetworkRequest);

            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.hasError()) {
                return null;
            }

            SubnetDTO responseNetworkDTO = new SubnetDTO();
            responseNetworkDTO.setSubnetName(subnetName);
            responseNetworkDTO.setIpv4Range(ipv4Range);

            Subnet subnet = new Subnet(subnetName, ipv4Range);
            subnetRepository.save(subnet);

            return responseNetworkDTO;

        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public SubnetDTO deleteSubnet(String subnetName)
        throws InterruptedException, ExecutionException, TimeoutException,
        IOException {
            try (SubnetworksClient subnetworksClient = SubnetworksClient.create()) {

                DeleteSubnetworkRequest deleteSubnetworkRequest = DeleteSubnetworkRequest.newBuilder()
                    .setProject(project)
                    .setRegion(region)
                    .setSubnetwork(subnetName)
                    .build();

                OperationFuture<Operation, Operation> operation = subnetworksClient.deleteAsync(deleteSubnetworkRequest);

                Operation response = operation.get(3, TimeUnit.MINUTES);

                if (response.hasError()) {
                    return null;
                }

                subnetRepository.deleteBySubnetName(subnetName);

                SubnetDTO subnetDTO = new SubnetDTO();
                subnetDTO.setSubnetName(subnetName);
                subnetDTO.setStatus(response.getStatus().name());

                return subnetDTO;
        }
    }

    @Override
    public List<SubnetDTO> listSubnet() throws IOException {
        try (SubnetworksClient subnetworksClient = SubnetworksClient.create()) {

            List<Subnet> subnetworks = subnetRepository.findAll();

            List<SubnetDTO> subnetDTOs = new ArrayList<SubnetDTO>();

            for (Subnet subnet : subnetworks) {
                SubnetDTO subnetDTO = new SubnetDTO();
                subnetDTO.setSubnetName(subnet.getSubnetName());
                subnetDTO.setIpv4Range(subnet.getIpv4Range());
                subnetDTOs.add(subnetDTO);
            }

            return subnetDTOs;

        }
    }

    @Override
    public SubnetDTO getSubnet(String subnetName) {
        Subnet subnet = subnetRepository.findBySubnetName(subnetName);

        SubnetDTO subnetDTO = new SubnetDTO();
        subnetDTO.setSubnetName(subnet.getSubnetName());
        subnetDTO.setIpv4Range(subnet.getIpv4Range());

        return subnetDTO;
    }
}
