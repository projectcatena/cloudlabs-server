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

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/compute")
public class ComputeController {

    @Autowired
    ComputeService computeService;

	// Create a new public instance with the provided "instanceName" value in the specified
	// project and zone.
	
	@DeleteMapping("/{project}/{zone}/{instanceName}")
	public String delete((@PathVariable String project, @PathVariable String zone, @PathVariable String instanceName))
			throws IOException, InterruptedException, ExecutionException, TimeoutException {

		try {
        	boolean deletionResult = computeService.deleteInstance(project, zone, instanceName);


            if (!deletionResult) {
			    return "{ \"status\": \"error\" }";
            }

			return "{ \"status\": \"success\" }";

		} 
	}
}