package com.cloudlabs.server.modules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
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

    /* 
    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        
        Module newModule = new Module();
        newModule.setModuleSubtitle("1_EH_000");
        newModule.setModuleName("Ethical Hacking");
        newModule.setModuleDescription("Explore various methods to extract valuable information from targeted systems without causing harm and gain practical experience in executing controlled attacks to simulate real-world scenarios, enabling the identification and mitigation of security weaknesses.");
        this.repository.save(newModule);
    }
    */

    @GetMapping
    public List<Module> getAllModules(){
        return this.repository.findAll();
    }

    @GetMapping("/{moduleId}")
    public Module getModuleById(@PathVariable String moduleId) {
        // Convert moduleId to Long
        Long moduleIdAsLong;
        try {
            moduleIdAsLong = Long.valueOf(moduleId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid module ID format");
        }

        // Find Module by its id
        Module module = this.repository.findByModuleId(moduleIdAsLong);

        // Exception if module does not exist
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }

        // Return the module
        return module;
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

        // Add the new module to the modules repository
        this.repository.save(newModule);

        // Return the created module
        return newModule;
    }

    @DeleteMapping("/delete/{moduleId}")
    public void deleteModule(@PathVariable String moduleId) {

        // Convert moduleId to Long
        Long moduleIdAsLong;
        try {
            moduleIdAsLong = Long.valueOf(moduleId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid module ID format");
        }

        // Find Module by its id
        Module module = this.repository.findByModuleId(moduleIdAsLong);

        // Exception if module does not exist
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }

        // Delete the module
        this.repository.delete(module);
    }

    @PutMapping("/update/{moduleId}")
    public Module updateModule(@PathVariable String moduleId, @RequestBody JsonNode requestData) {
        // Convert moduleId to Long
        Long moduleIdAsLong;
        try {
            moduleIdAsLong = Long.valueOf(moduleId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid module ID format");
        }

        // Find Module by its id
        Module module = this.repository.findByModuleId(moduleIdAsLong);

        // Exception if module does not exist
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }

        // Extract the form data from the JsonNode
        String subtitle = requestData.get("subtitle").asText();
        String title = requestData.get("title").asText();
        String description = requestData.get("description").asText();

        // Update the module details if provided
        if (!subtitle.isBlank()) {
            module.setModuleSubtitle(subtitle);
        }
        if (!title.isBlank()) {
            module.setModuleName(title);
        }
        if (!description.isBlank()) {
            module.setModuleDescription(description);
        }

        // Save the updated module
        this.repository.save(module);

        // Return the updated module
        return module;
    } 
}
