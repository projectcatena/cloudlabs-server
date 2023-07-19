package com.cloudlabs.server.module;

import com.cloudlabs.server.module.dto.ModuleDTO;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = { "${app.security.cors.origin}" })
@RestController
@RequestMapping("/Modules")
public class ModuleController {

    @Autowired
    ModuleService moduleService;

    @GetMapping
    public List<ModuleDTO> getAllModules() {

        List<ModuleDTO> response = moduleService.getAllModules();

        return response;
    }

    @GetMapping("/{moduleId}")
    public ModuleDTO getModuleById(@PathVariable String moduleId)
            throws IOException {
        Long moduleIdAsLong = Long.valueOf(moduleId);
        ModuleDTO response = moduleService.getModuleById(moduleIdAsLong);

        return response;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ModuleDTO addModule(@RequestBody ModuleDTO moduleDTO)
            throws IOException, InterruptedException, ExecutionException,
            TimeoutException {

        ModuleDTO response = moduleService.addModule(moduleDTO);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return response;
    }

    @DeleteMapping("/delete/{moduleId}")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ModuleDTO deleteModule(@PathVariable String moduleId)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException {

        Long moduleIdAsLong = Long.valueOf(moduleId);
        ModuleDTO response = moduleService.deleteModule(moduleIdAsLong);

        return response;
    }

    @PutMapping("/update/{moduleId}")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ModuleDTO updateModule(@PathVariable String moduleId,
            @RequestBody ModuleDTO moduleDTO)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException {

        Long moduleIdAsLong = Long.valueOf(moduleId);
        ModuleDTO response = moduleService.updateModule(moduleIdAsLong, moduleDTO);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return response;
    }

    @PostMapping("/add-users")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ModuleDTO addUsers(@RequestBody ModuleDTO moduleDTO)
            throws IOException, InterruptedException, ExecutionException,
            TimeoutException {

        ModuleDTO response = moduleService.addUsers(moduleDTO);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return response;
    }

    @PostMapping("/remove-users")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ModuleDTO removeUsers(@RequestBody ModuleDTO moduleDTO)
            throws IOException, InterruptedException, ExecutionException,
            TimeoutException {

        ModuleDTO response = moduleService.removeUsers(moduleDTO);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return response;
    }
}
