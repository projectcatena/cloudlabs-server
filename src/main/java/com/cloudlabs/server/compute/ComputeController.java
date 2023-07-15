package com.cloudlabs.server.compute;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import com.google.cloud.compute.v1.AttachedDisk;

@RestController
@RequestMapping("/compute")
public class ComputeController {

	@Autowired
	ComputeService computeService;

	// Create a new public instance with the provided "instanceName" value in the
	// specified project and zone.
	@PostMapping("/create")
	public ComputeDTO create(@RequestBody ComputeDTO computeDTO, AttachedDisk disk)
			throws IOException, InterruptedException, ExecutionException,
			TimeoutException {
		if(disk.getDiskSizeGb() == 0){
			disk = null;
			System.out.println(disk);
		}
		ComputeDTO response = computeService.createPublicInstance(computeDTO,disk);

		if (response == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@GetMapping("/list-machine-types")
	public List<MachineTypeDTO> listMachineTypes(@RequestParam(required = false) String query)
			throws IOException {

		List<MachineTypeDTO> response = computeService.listMachineTypes(query);

		return response;
	}

	@GetMapping("/list")
	public List<ComputeDTO> listComputeInstances() {

		List<ComputeDTO> response = computeService.listComputeInstances();

		return response;
	}

	@PostMapping("/instance")
	public ComputeDTO getComputeInstance(@RequestBody ComputeDTO computeDTO)
			throws IOException {

		ComputeDTO response = computeService.getComputeInstance(computeDTO.getInstanceName());

		return response;
	}

	@PostMapping("/delete")
	public ComputeDTO deleteComputeInstance(@RequestBody ComputeDTO computeDTO)
			throws InterruptedException, ExecutionException, TimeoutException,
			IOException {
		computeService.releaseStaticExternalIPAddress(
		String.format("%s-public-ip", computeDTO.getInstanceName()));
		System.out.println(computeDTO.getInstanceName());
		ComputeDTO response = computeService.deleteInstance(computeDTO.getInstanceName());
		

		return response;
	}
}
