package com.cloudlabs.server.compute;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.cloudlabs.server.compute.dto.AddressDTO;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;

public interface ComputeService {
    ComputeDTO createPublicInstance(ComputeDTO computeInstanceMetadata);
    ComputeDTO deleteInstance(String instanceName);
    void reserveStaticExternalIPAddress(String ipAddressName) throws InterruptedException, ExecutionException, IOException;
    AddressDTO getExternalStaticIPAdress(String addressName);
    void assignStaticExternalIPAddress(String instanceName, String ipAddress, String networkInterfaceName) throws IOException, InterruptedException, ExecutionException;
    void releaseStaticExternalIPAddress(String ipAddress) throws IOException;
    List<MachineTypeDTO> listMachineTypes(String query) throws IOException;
    List<ComputeDTO> listComputeInstances();
}
