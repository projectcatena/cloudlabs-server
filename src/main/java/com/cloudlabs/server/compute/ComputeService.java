package com.cloudlabs.server.compute;

import com.cloudlabs.server.compute.dto.AddressDTO;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ComputeService {
    ComputeDTO createPrivateInstance(ComputeDTO computeInstanceMetadata);

    ComputeDTO deleteInstance(String instanceName)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException;

    void reserveStaticExternalIPAddress(String ipAddressName)
            throws InterruptedException, ExecutionException, IOException;

    AddressDTO getInternalIpAddress(String instanceName);

    void assignStaticExternalIPAddress(String instanceName, String ipAddress,
            String networkInterfaceName)
            throws IOException, InterruptedException, ExecutionException;

    void releaseStaticExternalIPAddress(String ipAddressName) throws IOException;

    List<MachineTypeDTO> listMachineTypes(String query) throws IOException;

    List<ComputeDTO> listComputeInstances(Long moduleId);

    ComputeDTO getComputeInstance(String instanceName);

    ComputeDTO resetInstance(String instanceName)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException;

    ComputeDTO getInstanceStatus(String instanceName) throws IOException;

    ComputeDTO stopInstance(String instanceName)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException;

    ComputeDTO startInstance(String instanceName)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException;

    ComputeDTO addComputeInstanceUsers(ComputeDTO computeDTO);

    ComputeDTO removeComputeInstanceUsers(ComputeDTO computeDTO);

    ComputeDTO limitComputeRuntime(String instanceName, Long maxRunDuration)
            throws IOException, InterruptedException, ExecutionException,
            TimeoutException;
}
