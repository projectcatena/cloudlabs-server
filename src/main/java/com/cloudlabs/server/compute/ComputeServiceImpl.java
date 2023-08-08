package com.cloudlabs.server.compute;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.compute.dto.AddressDTO;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import com.cloudlabs.server.compute.dto.SourceImageDTO;
import com.cloudlabs.server.module.Module;
import com.cloudlabs.server.module.ModuleRepository;
import com.cloudlabs.server.module.dto.ModuleDTO;
import com.cloudlabs.server.subnet.Subnet;
import com.cloudlabs.server.subnet.SubnetRepository;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.dto.UserDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AddAccessConfigInstanceRequest;
import com.google.cloud.compute.v1.Address;
import com.google.cloud.compute.v1.AddressesClient;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDisk.Type;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.DeleteAddressRequest;
import com.google.cloud.compute.v1.DeleteInstanceRequest;
import com.google.cloud.compute.v1.GetInstanceRequest;
import com.google.cloud.compute.v1.GetZoneOperationRequest;
import com.google.cloud.compute.v1.InsertAddressRequest;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Items;
import com.google.cloud.compute.v1.ListMachineTypesRequest;
import com.google.cloud.compute.v1.MachineType;
import com.google.cloud.compute.v1.MachineTypesClient;
import com.google.cloud.compute.v1.Metadata;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.ResetInstanceRequest;
import com.google.cloud.compute.v1.ServiceAccount;
import com.google.cloud.compute.v1.StartInstanceRequest;
import com.google.cloud.compute.v1.StopInstanceRequest;
import com.google.cloud.compute.v1.ZoneOperationsClient;

@Service
public class ComputeServiceImpl implements ComputeService {
    // project-382920
    @Value("${gcp.project.id}")
    private String project;

    // asia-southeast1-c
    @Value("${gcp.project.zone}")
    private String zone;

    // asia-southeast1
    @Value("${gcp.project.region}")
    private String region;

    // vpc-name
    @Value("${gcp.project.vpc}")
    private String vpc;

    @Value("${gcp.startup.debian}")
    private String debianStartupScriptURL;

    @Value("${gcp.startup.windows}")
    private String windowsStartupScriptURL;

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SubnetRepository subnetRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Override
    public ComputeDTO createPrivateInstance(ComputeDTO computeInstanceMetadata) {
        // Initialize client that will be used to send requests. This client only
        // needs to be created once, and can be reused for multiple requests. After
        // completing all of your requests, call the `instancesClient.close()`
        // method on the client to safely clean up any remaining background
        // resources.
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
            computeInstanceMetadata.setDiskSizeGb(60); // 60GB minimum for Windows
            computeInstanceMetadata.setNetworkName("default");

            MachineTypeDTO machineTypeDTO = computeInstanceMetadata.getMachineType();
            machineTypeDTO.setZone(zone);
            String machineType = String.format("zones/%s/machineTypes/%s", machineTypeDTO.getZone(),
                    machineTypeDTO.getName());

            SourceImageDTO sourceImageDTO = computeInstanceMetadata.getSourceImage();

            String sourceImage;

            if (sourceImageDTO.getProject() == null) {
                // projects/cloudlabs-387310/global/images/windows-server-2019
                sourceImage = String.format("projects/%s/global/images/%s", project,
                        sourceImageDTO.getName());
            } else {
                sourceImage = String.format("projects/%s/global/images/family/%s",
                        sourceImageDTO.getProject(),
                        sourceImageDTO.getName());
            }

            long diskSizeGb = computeInstanceMetadata.getDiskSizeGb();
            String networkName = computeInstanceMetadata.getNetworkName();
            String instanceName = computeInstanceMetadata.getInstanceName();
            String subnetName = computeInstanceMetadata.getAddress().getSubnetName();
            String startupScript = computeInstanceMetadata.getStartupScript();
            Long maxRunDuration = computeInstanceMetadata.getMaxRunDuration();
            ModuleDTO moduleDTO = computeInstanceMetadata.getModule();

            if (startupScript == null) {
                startupScript = "";
            }

            if (moduleDTO == null || moduleDTO.getModuleId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "A module Id must be specifed");
            }

            // Minimum of 120 seconds otherwise instance fail to start
            if (maxRunDuration != null) {
                if (maxRunDuration < 120) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Minimum limit runtime duration is 120 seconds");
                }
            }

            // Retrive from DB to ensure data availability (validate first before
            // create on GCP) Get current user from security context
            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            String email = authenticationToken.getName();

            // Find user by email
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unrecoverable error, user not found"));

            // Find subnet by subnet name
            Subnet subnet = subnetRepository.findBySubnetName(subnetName);

            if (subnet == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Subnet does not exist");
            }

            Module module = moduleRepository.findById(moduleDTO.getModuleId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Module not found"));

            // Instance creation requires at least one persistent disk and one network
            // interface.
            AttachedDisk disk = AttachedDisk.newBuilder()
                    .setBoot(true)
                    .setAutoDelete(true)
                    .setType(Type.PERSISTENT.toString())
                    .setDeviceName("disk-1")
                    .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
                            .setSourceImage(sourceImage)
                            .setDiskSizeGb(diskSizeGb)
                            .build())
                    .build();

            // Use the network interface provided in the networkName argument.
            NetworkInterface networkInterface = NetworkInterface.newBuilder()
                    .setName(networkName)
                    .setNetwork(String.format("projects/%s/global/networks/%s",
                            project, vpc)) // VPC name
                    .setSubnetwork(String.format(
                            "projects/%s/regions/%s/subnetworks/%s", project, region,
                            subnetName)) // Subnet resource name
                    // .addAccessConfigs(addPublicIpAddressConfig)
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
        
        Items debianStartupScriptItem = Items.newBuilder()
        .setKey("startup-script-url")
        .setValue(debianStartupScriptURL)
        .build();

        Items windowsStartupScriptItem = Items.newBuilder()
        .setKey("windows-startup-script-url")
        .setValue(windowsStartupScriptURL)
        .build();

            Metadata metadata = Metadata.newBuilder().addAllItems(Arrays.asList(items,debianStartupScriptItem,windowsStartupScriptItem)).build();

            // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to
            // an instance.
            Instance instanceResource = Instance.newBuilder()
                    .setName(instanceName)
                    .setMachineType(machineType)
                    .setMetadata(metadata)
                    .addServiceAccounts(serviceAccount)
                    .addDisks(disk)
                    .addNetworkInterfaces(networkInterface)
                    .build();

            // Insert the instance in the specified project and zone.
            InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstanceResource(instanceResource)
                    .build();

            OperationFuture<Operation, Operation> operation = instancesClient.insertAsync(insertInstanceRequest);

            // Wait for the operation to complete.
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.hasError()) {
                return null;
            }

            // Get instance dynamically assigned private IP after creation
            AddressDTO addressDTO = getInternalIpAddress(instanceName);

            ComputeDTO responseComputeDTO = new ComputeDTO();
            responseComputeDTO.setInstanceName(instanceName);
            responseComputeDTO.setAddress(addressDTO);

            // Successful Instance Creation, save Compute and Current User to
            // Database
            Compute compute = new Compute(instanceName, machineTypeDTO.getName(),
                    addressDTO.getPrivateIPv4Address(),
                    diskSizeGb, sourceImage, null, subnet);

            Set<User> users = new HashSet<User>();
            users.add(user);

            compute.setUsers(users);
            compute.setModule(module);
            computeRepository.save(compute);

            // Minimum of 120 seconds otherwise instance fail to start
            if (!(maxRunDuration == null || maxRunDuration < 120)) {
                // Then, set limit runtime
                ComputeDTO limitRuntimeResponseDTO = limitComputeRuntime(instanceName, maxRunDuration);
                responseComputeDTO.setMaxRunDuration(
                        limitRuntimeResponseDTO.getMaxRunDuration());
            }

            return responseComputeDTO;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public ComputeDTO createPrivateInstance(ComputeDTO computeInstanceMetadata,
            AttachedDisk disk) {
        // Initialize client that will be used to send requests. This client only
        // needs to be created once, and can be reused for multiple requests. After
        // completing all of your requests, call the `instancesClient.close()`
        // method on the client to safely clean up any remaining background
        // resources.
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
            computeInstanceMetadata.setDiskSizeGb(60); // 60GB minimum for Windows
            computeInstanceMetadata.setNetworkName("default");

            MachineTypeDTO machineTypeDTO = computeInstanceMetadata.getMachineType();
            machineTypeDTO.setZone(zone);
            String machineType = String.format("zones/%s/machineTypes/%s", machineTypeDTO.getZone(),
                    machineTypeDTO.getName());

            SourceImageDTO sourceImageDTO = computeInstanceMetadata.getSourceImage();

            String sourceImage;

            if (sourceImageDTO.getProject() == null) {
                // projects/cloudlabs-387310/global/images/windows-server-2019
                sourceImage = String.format("projects/%s/global/images/%s", project,
                        sourceImageDTO.getName());
            } else {
                sourceImage = String.format("projects/%s/global/images/family/%s",
                        sourceImageDTO.getProject(),
                        sourceImageDTO.getName());
            }

            long diskSizeGb = computeInstanceMetadata.getDiskSizeGb();
            String networkName = computeInstanceMetadata.getNetworkName();
            String instanceName = computeInstanceMetadata.getInstanceName();
            String subnetName = computeInstanceMetadata.getAddress().getSubnetName();
            String startupScript = computeInstanceMetadata.getStartupScript();
            Long maxRunDuration = computeInstanceMetadata.getMaxRunDuration();
            ModuleDTO moduleDTO = computeInstanceMetadata.getModule();

            if (startupScript == null) {
                startupScript = "";
            }

            if (moduleDTO == null || moduleDTO.getModuleId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "A module Id must be specifed");
            }

            // Minimum of 120 seconds otherwise instance fail to start
            if (maxRunDuration != null) {
                if (maxRunDuration < 120) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Minimum limit runtime duration is 120 seconds");
                }
            }

            Module module = moduleRepository.findById(moduleDTO.getModuleId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Module not found"));

            // Retrive from DB to ensure data availability (validate first before
            // create on GCP) Get current user from security context
            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            String email = authenticationToken.getName();

            // Find user by email
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unrecoverable error, user not found"));

            // Find subnet by subnet name
            Subnet subnet = subnetRepository.findBySubnetName(subnetName);

            if (subnet == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Subnet does not exist");
            }

            // Instance creation requires at least one persistent disk and one network
            // interface.
            Vector<AttachedDisk> disks = new Vector<>();
            disks.add(disk);

            // Reserve Public IP Address for the instance
            // String addressResourceName = String.format("%s-public-ip",
            // instanceName); reserveStaticExternalIPAddress(addressResourceName);

            // Assign created public IP at instance creation
            // AccessConfig addPublicIpAddressConfig = AccessConfig.newBuilder()
            // .setNatIP(publicIPAddressDTO.getIpv4Address())
            // .build();

            // Use the network interface provided in the networkName argument.
            NetworkInterface networkInterface = NetworkInterface.newBuilder()
                    .setName(networkName)
                    .setNetwork(String.format("projects/%s/global/networks/%s",
                            project, vpc)) // VPC name
                    .setSubnetwork(String.format(
                            "projects/%s/regions/%s/subnetworks/%s", project, region,
                            subnetName)) // Subnet resource name
                    // .addAccessConfigs(addPublicIpAddressConfig)
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

            Metadata metadata = Metadata.newBuilder().addItems(items).build();

            // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to
            // an instance.
            Instance instanceResource = Instance.newBuilder()
                    .setName(instanceName)
                    .setMachineType(machineType)
                    .setMetadata(metadata)
                    .addServiceAccounts(serviceAccount)
                    .addAllDisks(disks)
                    .addNetworkInterfaces(networkInterface)
                    .build();

            // System.out.printf("Creating instance: %s at %s %n", instanceName,
            // zone);

            // Insert the instance in the specified project and zone.
            InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstanceResource(instanceResource)
                    .build();

            OperationFuture<Operation, Operation> operation = instancesClient.insertAsync(insertInstanceRequest);

            // Wait for the operation to complete.
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.hasError()) {
                return null;
            }

            // Get instance dynamically assigned private IP after creation
            AddressDTO addressDTO = getInternalIpAddress(instanceName);

            ComputeDTO responseComputeDTO = new ComputeDTO();
            responseComputeDTO.setInstanceName(instanceName);
            responseComputeDTO.setAddress(addressDTO);

            // Successful Instance Creation, save Compute and Current User to
            // Database
            Compute compute = new Compute(instanceName, machineTypeDTO.getName(),
                    addressDTO.getPrivateIPv4Address(),
                    diskSizeGb, sourceImage, null, subnet);

            Set<User> users = new HashSet<User>();
            users.add(user);

            compute.setUsers(users);
            compute.setModule(module);
            computeRepository.save(compute);

            // Minimum of 120 seconds otherwise instance fail to start
            if (maxRunDuration != null) {
                // Then, set limit runtime
                ComputeDTO limitRuntimeResponseDTO = limitComputeRuntime(instanceName, maxRunDuration);
                responseComputeDTO.setMaxRunDuration(
                        limitRuntimeResponseDTO.getMaxRunDuration());
            }

            return responseComputeDTO;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public ComputeDTO deleteInstance(String instanceName)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException {
        try (InstancesClient instancesClient = InstancesClient.create()) {

            // Describe which instance is to be deleted.
            DeleteInstanceRequest deleteInstanceRequest = DeleteInstanceRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstance(instanceName)
                    .build();

            OperationFuture<Operation, Operation> operation = instancesClient.deleteAsync(deleteInstanceRequest);
            // Wait for the operation to complete.
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.hasError()) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Deletion from GCP has failed unrecoverably.");
            }

            Long deletionCount = computeRepository.deleteByInstanceName(instanceName);

            if (deletionCount == 0) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Deletion from database has failed unrecoverably.");
            }

            ComputeDTO computeDTO = new ComputeDTO();
            computeDTO.setInstanceName(instanceName);
            computeDTO.setStatus(response.getStatus().name());

            return computeDTO;
        }
    }

    @Override
    public final void reserveStaticExternalIPAddress(String ipAddressName)
            throws InterruptedException, ExecutionException, IOException {
        try (AddressesClient addressClient = AddressesClient.create()) {

            Address addressResource = Address.newBuilder().setName(ipAddressName).build();

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
    // public AddressDTO getExternalStaticIPAdress(String addressName) {
    // try (AddressesClient addressClient = AddressesClient.create()) {
    // Address address = addressClient.get(project, region, addressName);
    //
    // AddressDTO addressDTO = new AddressDTO();
    // addressDTO.setIpv4Address(address.getAddress());
    // addressDTO.setName(addressName);
    //
    // return addressDTO;
    // } catch (Exception exception) {
    // return null;
    // }
    // }

    /**
     * Get internal or private IP address of the instance. The address is
     * dynamically assigned upon creation.
     *
     */
    public AddressDTO getInternalIpAddress(String instanceName) {
        try (InstancesClient instancesClient = InstancesClient.create()) {

            GetInstanceRequest getInstanceRequest = GetInstanceRequest.newBuilder()
                    .setZone(zone)
                    .setProject(project)
                    .setInstance(instanceName)
                    .build();

            Instance instance = instancesClient.get(getInstanceRequest);

            // Get default interface
            NetworkInterface networkInterface = instance.getNetworkInterfaces(0);

            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setPrivateIPv4Address(networkInterface.getNetworkIP());
            addressDTO.setSubnetName(networkInterface.getSubnetwork());

            return addressDTO;
        } catch (Exception exception) {
            return null;
        }
    }

    public void assignStaticExternalIPAddress(String instanceName,
            String ipAddress,
            String networkInterfaceName)
            throws IOException, InterruptedException, ExecutionException {

        try (InstancesClient instancesClient = InstancesClient.create()) {

            AccessConfig accessConfig = AccessConfig.newBuilder()
                    .setName("External NAT")
                    .setNatIP(ipAddress)
                    .build();

            AddAccessConfigInstanceRequest request = AddAccessConfigInstanceRequest.newBuilder()
                    .setAccessConfigResource(accessConfig)
                    .setInstance(instanceName)
                    .setNetworkInterface(
                            networkInterfaceName) // value should be network
                                                  // interface name (e.g. nic0)
                    .setProject(project)
                    .setZone(zone)
                    .build();

            // Operation response =
            // instancesClient.addAccessConfigAsync(request).get();
            instancesClient.addAccessConfigAsync(request).get();
        }
    }

    @Override
    public final void releaseStaticExternalIPAddress(String ipAddressName)
            throws IOException {
        try (AddressesClient addressClient = AddressesClient.create()) {

            DeleteAddressRequest request = DeleteAddressRequest.newBuilder()
                    .setAddress(ipAddressName)
                    .setProject(project)
                    .setRegion(region)
                    .build();

            // Operation response = addressClient.insertAsync(request).get();
            addressClient.deleteAsync(request);
        }
    }

    @Override
    public List<MachineTypeDTO> listMachineTypes(String query)
            throws IOException {
        try (MachineTypesClient machineTypesClient = MachineTypesClient.create()) {

            ListMachineTypesRequest listMachineTypesRequest = ListMachineTypesRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setMaxResults(10)
                    .setFilter(
                            (query == null) ? "" : String.format("name eq ^%s.*", query))
                    .build();

            List<MachineTypeDTO> machineTypes = new ArrayList<MachineTypeDTO>();
            for (MachineType machineType : machineTypesClient.list(listMachineTypesRequest).iterateAll()) {
                MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
                machineTypeDTO.setName(machineType.getName());
                machineTypes.add(machineTypeDTO);
            }

            return machineTypes;
        }
    }

    @Override
    public List<ComputeDTO> listComputeInstances(Long moduleId) {

        // Get email from Jwt token using context
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authenticationToken.getName();

        List<Compute> computeInstances = (moduleId == null)
                ? computeRepository.findByUsers_Email(email)
                : computeRepository.findByUsers_EmailAndModuleId(email, moduleId);

        List<ComputeDTO> computeDTOs = new ArrayList<ComputeDTO>();

        for (Compute compute : computeInstances) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setPrivateIPv4Address(compute.getPrivateIPv4Address());
            addressDTO.setSubnetName(compute.getSubnet().getSubnetName());

            MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
            machineTypeDTO.setName(compute.getMachineType());

            ComputeDTO computeDTO = new ComputeDTO();
            computeDTO.setAddress(addressDTO);
            computeDTO.setInstanceName(compute.getInstanceName());
            computeDTO.setMachineType(machineTypeDTO);

            computeDTOs.add(computeDTO);
        }

        return computeDTOs;
    }

    @Override
    public ComputeDTO getComputeInstance(String instanceName) {

        // Get email from Jwt token using context
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authenticationToken.getName();

        // Ensure that only assigned users can see the instance
        Compute compute = computeRepository.findByUsers_EmailAndInstanceName(email, instanceName)
                .orElse(null);

        if (compute == null) {
            return null;
        }

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setPrivateIPv4Address(compute.getPrivateIPv4Address());
        addressDTO.setSubnetName(compute.getSubnet().getSubnetName());

        MachineTypeDTO machineTypeDTO = new MachineTypeDTO();
        machineTypeDTO.setName(compute.getMachineType());

        SourceImageDTO sourceImageDTO = new SourceImageDTO();
        sourceImageDTO.setName(compute.getSourceImage());

        ComputeDTO computeDTO = new ComputeDTO();
        computeDTO.setInstanceName(compute.getInstanceName());
        computeDTO.setAddress(addressDTO);
        computeDTO.setMachineType(machineTypeDTO);
        computeDTO.setSourceImage(sourceImageDTO);
        computeDTO.setDiskSizeGb(compute.getDiskSizeGb());

        ModuleDTO moduleDTO = new ModuleDTO();
        moduleDTO.setModuleId(compute.getModule().getModuleId());
        computeDTO.setModule(moduleDTO);

        return computeDTO;
    }

    @Override
    public ComputeDTO resetInstance(String instanceName)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException {
        try (InstancesClient instancesClient = InstancesClient.create()) {

            ResetInstanceRequest resetInstanceRequest = ResetInstanceRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstance(instanceName)
                    .build();

            OperationFuture<Operation, Operation> operation = instancesClient.resetAsync(resetInstanceRequest);
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.getStatus() == Status.DONE) {
                System.out.println("Instance reset successfully ! ");
            }

            ComputeDTO computeDTO = new ComputeDTO();
            computeDTO.setInstanceName(instanceName);
            computeDTO.setStatus(response.getStatus().name());
            return computeDTO;
        }
    }

    @Override
    public ComputeDTO getInstanceStatus(String instanceName) throws IOException {
        try (InstancesClient instancesClient = InstancesClient.create()) {
            GetInstanceRequest request = GetInstanceRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstance(instanceName)
                    .build();

            Instance response = instancesClient.get(request);

            ComputeDTO computeDTO = new ComputeDTO();
            computeDTO.setStatus(response.getStatus());
            return computeDTO;
        }
    }

    @Override
    public ComputeDTO stopInstance(String instanceName)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException {
        try (InstancesClient instancesClient = InstancesClient.create()) {

            StopInstanceRequest stopInstanceRequest = StopInstanceRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstance(instanceName)
                    .build();

            OperationFuture<Operation, Operation> operation = instancesClient.stopAsync(stopInstanceRequest);
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.getStatus() == Status.DONE) {
                System.out.println("Instance stopped successfully ! ");
            }

            ComputeDTO computeDTO = new ComputeDTO();
            computeDTO.setInstanceName(instanceName);
            computeDTO.setStatus(response.getStatus().name());
            return computeDTO;
        }
    }

    @Override
    public ComputeDTO startInstance(String instanceName)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException {
        try (InstancesClient instancesClient = InstancesClient.create()) {

            StartInstanceRequest startInstanceRequest = StartInstanceRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstance(instanceName)
                    .build();

            OperationFuture<Operation, Operation> operation = instancesClient.startAsync(startInstanceRequest);
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.getStatus() == Status.DONE) {
                System.out.println("Instance started successfully ! ");
            }

            ComputeDTO computeDTO = new ComputeDTO();
            computeDTO.setInstanceName(instanceName);
            computeDTO.setStatus(response.getStatus().name());
            return computeDTO;
        }
    }

    /*
     * Allow tutor to assign users to a compute instance
     *
     * @param users
     *
     * @param computeInstance
     */
    @Override
    public ComputeDTO addComputeInstanceUsers(ComputeDTO computeDTO) {
        Optional<Compute> compute = computeRepository.findByInstanceName(computeDTO.getInstanceName());

        if (compute.isEmpty()) {
            return null;
        }

        List<UserDTO> addedUsers = new ArrayList<>();
        Set<User> users = new HashSet<>();

        for (UserDTO userDTO : computeDTO.getUsers()) {
            User user = userRepository.findByEmail(userDTO.getEmail())
                    .orElseThrow(
                            () -> new UsernameNotFoundException("User not found!"));
            users.add(user);
            addedUsers.add(userDTO);
        }

        compute.get().getUsers().addAll(users);
        computeRepository.save(compute.get());

        computeDTO.setUsers(addedUsers);

        return computeDTO;
    }

    /*
     * Allow tutor to remove/unassign users from a compute instance entity
     */
    @Override
    public ComputeDTO removeComputeInstanceUsers(ComputeDTO computeDTO) {

        Optional<Compute> compute = computeRepository.findByInstanceName(computeDTO.getInstanceName());

        if (compute.isEmpty()) {
            return null;
        }
        // Get list of users from entity
        Set<User> users = compute.get().getUsers();

        List<UserDTO> userDTOs = new ArrayList<>();

        // Get a list of users to remove
        for (UserDTO userDTO : computeDTO.getUsers()) {
            // Ensure valid user
            User user = userRepository.findByEmail(userDTO.getEmail())
                    .orElseThrow(
                            () -> new UsernameNotFoundException("User not found!"));

            // If is in list of assigned users, consider valid
            if (users.contains(user)) {
                users.remove(user);

                // Add to list of users removed
                userDTOs.add(userDTO);
            }
        }

        // Flush changes to database
        compute.get().setUsers(users);
        computeRepository.save(compute.get());

        computeDTO.setUsers(userDTOs);

        return computeDTO;
    }

    @Override
    public ComputeDTO limitComputeRuntime(String instanceName,
            Long maxRunDuration)
            throws IOException, InterruptedException, ExecutionException,
            TimeoutException {

        // Get access token from ADC
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        AccessToken accessToken = credentials.refreshAccessToken();

        // Stop VM
        stopInstance(instanceName);

        // Update Scheduling
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(
                        "https://compute.googleapis.com/compute/beta/projects/%s/zones/%s/instances/%s/setScheduling",
                        project, zone, instanceName)))
                .POST(HttpRequest.BodyPublishers.ofString(String.format(
                        "{\"maxRunDuration\":{\"seconds\":\"%s\"},\"instanceTerminationAction\":\"STOP\"}",
                        maxRunDuration))) // unit is in seconds
                .header("Authorization", "Bearer " + accessToken.getTokenValue())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode operationJson = objectMapper.readTree(response.body());

        // Check resource is done updating by polling every 10 seconds
        try (ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {

            while (true) {
                // Compute instances are zone-based
                GetZoneOperationRequest operationRequest = GetZoneOperationRequest.newBuilder()
                        .setOperation(
                                operationJson.get("name").asText()) // name of operation
                        .setProject(project)
                        .setZone(zone)
                        .build();

                Operation operationResponse = zoneOperationsClient.get(operationRequest);

                if (operationResponse.getStatus().equals(Operation.Status.DONE)) {
                    System.out.println(operationResponse.getStatus());
                    break;
                }

                Thread.sleep(10000);
            }
        }

        // Afer set scheduling, start instance
        startInstance(instanceName);

        ComputeDTO computeDTO = new ComputeDTO();
        computeDTO.setInstanceName(instanceName);
        computeDTO.setMaxRunDuration(maxRunDuration);

        return computeDTO;
    }
}
