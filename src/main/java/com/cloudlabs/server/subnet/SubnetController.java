package com.cloudlabs.server.subnet;

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

import com.cloudlabs.server.subnet.dto.SubnetDTO;

@RestController
@RequestMapping("/network")
public class SubnetController {
    
    @Autowired
    SubnetService subnetService;

    @PostMapping("create")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public SubnetDTO create(@RequestBody SubnetDTO subnetDTO)
            throws IOException, InterruptedException, ExecutionException,
            TimeoutException {
                SubnetDTO response = subnetService.createSubnet(subnetDTO);

                if (response == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                }

                return response;
            }

    @PostMapping("delete")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public SubnetDTO delete(@RequestBody SubnetDTO subnetDTO)
            throws IOException, InterruptedException, ExecutionException,
            TimeoutException {
                SubnetDTO response = subnetService.deleteSubnet(subnetDTO.getSubnetName());

                if (response == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }

                return response;
            }
}
