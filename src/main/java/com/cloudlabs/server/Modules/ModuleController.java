package com.cloudlabs.server.modules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cloudlabs.server.modules.Module;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.swing.JSpinner.ListEditor;

@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RestController
@RequestMapping("/Modules")
public class ModuleController {

    @Autowired
    private ModuleRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        List allModules = this.repository.findAll();

        Module newModule = new Module();
        newModule.setModuleSubtitle("1_EH_000");
        newModule.setModuleName("Ethical Hacking");
        newModule.setModuleDescription("Explore various methods to extract valuable information from targeted systems without causing harm and gain practical experience in executing controlled attacks to simulate real-world scenarios, enabling the identification and mitigation of security weaknesses.");
        this.repository.save(newModule);

        allModules = this.repository.findAll();
    }

    @GetMapping
    public List<Module> getAllModules(){
        return repository.findAll();
    }

    @PostMapping("/create")
    public Module addModule(@RequestBody JsonNode requestData) {
        // Extract the form data from the JsonNode
        String subtitle = requestData.get("subtitle").asText();
        String title = requestData.get("title").asText();
        String description = requestData.get("description").asText();

        if (subtitle.isBlank() || title.isBlank() || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Module details input");
        }

        // Create a new Module instance
        Module newModule = new Module();

        // Set the attributes of the module
        newModule.setModuleSubtitle(subtitle);
        newModule.setModuleName(title);
        newModule.setModuleDescription(description);

        // Generate a unique ID for the module
        Long id = Long.valueOf(UUID.randomUUID().toString());
        newModule.setModuleId(id);

        // Add the new module to the modules ArrayList
        this.repository.save(newModule);

        // Return the created module
        return newModule;
    }
}
