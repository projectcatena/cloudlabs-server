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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RestController
@RequestMapping("/Modules")
public class ModuleController {
    
    @Autowired
    ModuleService moduleService;

    @GetMapping
    public List<Module> getAllModules(){
        return moduleService.getAllModules();
    }

    @GetMapping("/{moduleId}")
    public Module getModuleById(@PathVariable String moduleId) {
        try {
            Long moduleIdAsLong = Long.valueOf(moduleId);
            return moduleService.getModuleById(moduleIdAsLong);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid module ID format");
        }
    }

    @PostMapping("/create")
    public Module addModule(@RequestBody JsonNode requestData) {
        String subtitle = requestData.get("subtitle").asText();
        String title = requestData.get("title").asText();
        String description = requestData.get("description").asText();

        if (subtitle.isBlank() || title.isBlank() || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Module details input");
        }

        return moduleService.addModule(subtitle, title, description);
    }

    @DeleteMapping("/delete/{moduleId}")
    public void deleteModule(@PathVariable String moduleId) {
        try {
            Long moduleIdAsLong = Long.valueOf(moduleId);
            moduleService.deleteModule(moduleIdAsLong);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid module ID format");
        }
    }

    @PutMapping("/update/{moduleId}")
    public Module updateModule(@PathVariable String moduleId, @RequestBody JsonNode requestData) {
        try {
            Long moduleIdAsLong = Long.valueOf(moduleId);
            String subtitle = requestData.get("subtitle").asText();
            String title = requestData.get("title").asText();
            String description = requestData.get("description").asText();

            return moduleService.updateModule(moduleIdAsLong, subtitle, title, description);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid module ID format");
        }
    }
}
