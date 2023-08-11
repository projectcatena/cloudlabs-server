package com.cloudlabs.server.subnet;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.cloudlabs.server.subnet.dto.SubnetDTO;
import com.google.cloud.compute.v1.Network;

public interface SubnetService {
    SubnetDTO createSubnet(SubnetDTO subnetDTO);
    SubnetDTO deleteSubnet(String subnetName) throws InterruptedException, ExecutionException, TimeoutException,
    IOException;
    List<SubnetDTO> listSubnet() throws IOException;
    SubnetDTO getSubnet(String subnetName) throws IOException;
    void createFirewallRule(String ipv4Range, Network network, String project, String firewallRuleName) throws IOException, ExecutionException, InterruptedException, TimeoutException;
    void deleteFirewallRule(String project, String firewallRuleName) throws IOException, ExecutionException, InterruptedException, TimeoutException;
}
