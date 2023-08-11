package com.cloudlabs.server.module;

import com.cloudlabs.server.module.dto.ModuleDTO;
import com.cloudlabs.server.user.dto.UserDTO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('USER','TUTOR','ADMIN')")
    public List<ModuleDTO> getUserModules() {

        List<ModuleDTO> response = moduleService.getUserModules();

        return response;
    }

    @GetMapping("/{id}")
    public ModuleDTO getModuleById(@PathVariable String id)
            throws IOException {
        Long idAsLong = Long.valueOf(id);
        ModuleDTO response = moduleService.getModuleById(idAsLong);

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

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ModuleDTO deleteModule(@PathVariable String id)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException {

        Long idAsLong = Long.valueOf(id);
        ModuleDTO response = moduleService.deleteModule(idAsLong);

        return response;
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ModuleDTO updateModule(@PathVariable String id,
            @RequestBody ModuleDTO moduleDTO)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException {

        Long idAsLong = Long.valueOf(id);
        ModuleDTO response = moduleService.updateModule(idAsLong, moduleDTO);

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

    @PostMapping("/list-users")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public List<UserDTO> listUsers(@RequestBody ModuleDTO moduleDTO)
            throws IOException, InterruptedException, ExecutionException,
            TimeoutException {

        List<UserDTO> response = moduleService.listUsers(moduleDTO);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return response;
    }

    @PostMapping("/add-computes")
    public ModuleDTO addModuleComputeInstance(@RequestBody ModuleDTO moduleDTO) {
        ModuleDTO response = moduleService.addModuleComputeInstance(moduleDTO);
        return response;
    }

    @PostMapping("/remove-computes")
    public ModuleDTO removeModuleComputeInstance(@RequestBody ModuleDTO moduleDTO) {
        ModuleDTO response = moduleService.removeModuleComputeInstance(moduleDTO);
        return response;
    }
}
