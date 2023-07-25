package com.cloudlabs.server.subnet;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.cloudlabs.server.subnet.dto.SubnetDTO;

public interface SubnetService {
    SubnetDTO createSubnet(SubnetDTO subnetDTO);
    SubnetDTO deleteSubnet(String subnetName) throws InterruptedException, ExecutionException, TimeoutException,
    IOException;
}
