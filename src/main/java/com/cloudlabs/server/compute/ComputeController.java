package com.cloudlabs.server.compute;

import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.compute.dto.MachineTypeDTO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/compute")
public class ComputeController {

	@Autowired
	ComputeService computeService;

	// Create a new public instance with the provided "instanceName" value in the
	// specified project and zone.
	@PostMapping("/create")
	@PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
	public ComputeDTO create(@RequestBody ComputeDTO computeDTO)
			throws IOException, InterruptedException, ExecutionException,
			TimeoutException {

		ComputeDTO response = computeService.createPublicInstance(computeDTO);

		if (response == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@GetMapping("/list-machine-types")
	@PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
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

	@GetMapping("/instance")
	public ComputeDTO getComputeInstance(@RequestParam String instanceName)
			throws IOException {

		ComputeDTO response = computeService.getComputeInstance(instanceName);

		return response;
	}

	@PostMapping("/delete")
	@PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
	public ComputeDTO deleteComputeInstance(@RequestBody ComputeDTO computeDTO)
			throws InterruptedException, ExecutionException, TimeoutException,
			IOException {
		ComputeDTO response = computeService.deleteInstance(computeDTO.getInstanceName());
		computeService.releaseStaticExternalIPAddress(
				String.format("%s-public-ip", response.getInstanceName()));

		return response;
	}

	@PostMapping("/reset")
	public ComputeDTO resetInstance(@RequestBody ComputeDTO resetRequest) throws InterruptedException, ExecutionException, TimeoutException, IOException {
		
		ComputeDTO response = computeService.resetInstance(resetRequest.getInstanceName());

		return response;
	}

	@PostMapping("/status")
	public ComputeDTO getInstanceStatus(@RequestBody ComputeDTO getStatus) throws IOException {

		ComputeDTO response = computeService.getInstanceStatus(getStatus.getInstanceName());

		return response;
	}

	@PostMapping("/stop")
	public ComputeDTO stopInstance(@RequestBody ComputeDTO stopRequest) throws InterruptedException, ExecutionException, TimeoutException, IOException {

		ComputeDTO response = computeService.stopInstance(stopRequest.getInstanceName());

		return response;
	}

	@PostMapping("/start")
	public ComputeDTO startInstance(@RequestBody ComputeDTO startRequest) throws InterruptedException, ExecutionException, TimeoutException, IOException {

		ComputeDTO response = computeService.startInstance(startRequest.getInstanceName());

		return response;
	}

}
