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
	@PostMapping("/create")
	public String create(@RequestBody JsonNode request)
			throws IOException, InterruptedException, ExecutionException, TimeoutException {

		try {
			String machineType = String.format("zones/asia-southeast1-b/machineTypes/%s",
					request.get("selectedInstanceType").get("name").asText());
			String sourceImage = String
					.format("%s%s", request.get("selectedImage").get("project").asText(),
							request.get("selectedImage").get("name").asText());
			long diskSizeGb = 10L;
			String networkName = "default";
			String instanceName = request.get("name").asText();
			String startupScript = request.get("script").asText();

            Compute computeInstanceMetadata = new Compute(machineType, sourceImage, diskSizeGb, networkName, instanceName, startupScript);

            boolean isSuccess = computeService.createPublicInstance(computeInstanceMetadata);

            if (!isSuccess) {
			    return "{ \"status\": \"error\" }";
            }

			return "{ \"status\": \"success\" }";

		} catch (IllegalArgumentException illegalArgumentException) {
			// Should implement custom exception handler, as "server.error.include-message=always" 
			// workaround may disclose sensitive internal exceptions
			// Source: https://stackoverflow.com/questions/62561211/spring-responsestatusexception-does-not-return-reason
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal parameters.");
		} catch (Exception exception) {
            // Generic, catch-all exception (not good, but it works now)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal parameters.");
		}
	}
}