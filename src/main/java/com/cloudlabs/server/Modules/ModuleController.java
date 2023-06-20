package com.cloudlabs.server.Modules;

import com.cloudlabs.server.Modules.Module;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@CrossOrigin(origins = {"${app.security.cors.origin}"})
@RestController
@RequestMapping("/Modules")
public class ModuleController {
    
    private List<Module> modules = new ArrayList<>();

    public ModuleController() {
        modules.add(Module.from("1_EH_00", "Ethical Hacking", "Explore various methods to extract valuable information from targeted systems without causing harm and gain practical experience in executing controlled attacks to simulate real-world scenarios, enabling the identification and mitigation of security weaknesses."));
        modules.add(Module.from("1_WAPT_00", "Web Application Pen-Testing", "Delve into the process of identifying and exploiting vulnerabilities in web applications to assess their resilience against cyberattacks and gain hands-on experience with tools and techniques specifically designed for web application testing, such as vulnerability scanners, proxy tools, and manual testing methodologies."));
    }

    @GetMapping
    public List<Module> getModules(){
        return modules;
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
        Module module = new Module();

        // Set the attributes of the module
        module.setModuleSubtitle(subtitle);
        module.setModuleName(title);
        module.setModuleDescription(description);

        // Generate a unique ID for the module
        String id = UUID.randomUUID().toString();
        module.setModuleId(id);

        // Add the new module to the modules ArrayList
        modules.add(module);

        // Return the created module
        return module;
    }
}
