package com.cloudlabs.server.compute;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.compute.dto.ComputeDTO;

@RestController
@RequestMapping("/compute")
public class ComputeController {

    @Autowired
    ComputeService computeService;

	// Create a new public instance with the provided "instanceName" value in the specified
	// project and zone.
	@PostMapping("/create")
	public ComputeDTO create(@RequestBody ComputeDTO computeDTO) throws IOException, InterruptedException, ExecutionException, TimeoutException {

		ComputeDTO response = computeService.createPublicInstance(computeDTO);

		if (response == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		return response;
	}
}