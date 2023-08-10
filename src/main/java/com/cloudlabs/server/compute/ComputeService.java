package com.cloudlabs.server.compute;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.cloudlabs.server.compute.dto.AddressDTO;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import com.cloudlabs.server.user.dto.UserDTO;
import com.google.cloud.compute.v1.AttachedDisk;

public interface ComputeService {
    ComputeDTO createPrivateInstance(ComputeDTO computeInstanceMetadata);

    ComputeDTO createPrivateInstance(ComputeDTO computeInstanceMetadata, AttachedDisk disk);

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

    List<ComputeDTO> listAllComputeInstances();

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

    List<UserDTO> getAllUsers();

    ComputeDTO limitComputeRuntime(String instanceName, Long maxRunDuration)
            throws IOException, InterruptedException, ExecutionException,
            TimeoutException;
}
