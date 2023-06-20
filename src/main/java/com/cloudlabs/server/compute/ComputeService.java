package com.cloudlabs.server.compute;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ComputeService {
    boolean createPublicInstance(Compute computeInstanceMetadata) throws IOException, InterruptedException, ExecutionException, TimeoutException;
    String reserveStaticExternalIPAddress(String project, String region, String ipAddressName);
    String getExternalStaticIPAdress(String project, String region, String addressName);
    String assignStaticExternalIPAddress(String project, String zone, String instanceName, String ipAddress, String networkInterfaceName) throws IOException;
}
