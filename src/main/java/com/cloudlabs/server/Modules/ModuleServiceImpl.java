package com.cloudlabs.server.modules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleRepository repository;

    @Override
    public List<Module> getAllModules() {
        List<Module> modules = repository.findAll();
        return modules.stream()
                  .sorted(Comparator.comparing(Module::getModuleName, String.CASE_INSENSITIVE_ORDER))
                  .collect(Collectors.toList());
    }

    @Override
    public Module getModuleById(Long moduleId) {
        Module module = this.repository.findByModuleId(moduleId);
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }
        return module;
    }

    @Override
    public Module addModule(String subtitle, String title, String description) {
        if (subtitle.isBlank() || title.isBlank() || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Module details input");
        }
        Module newModule = new Module(subtitle, title, description);
        this.repository.save(newModule);
        return newModule;
    }

    @Override
    public void deleteModule(Long moduleId) {
        Module module = this.repository.findByModuleId(moduleId);
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }
        this.repository.delete(module);
    }

    @Override
    public Module updateModule(Long moduleId, String subtitle, String title, String description) {
        Module module = this.repository.findByModuleId(moduleId);
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }

        if (!subtitle.isBlank()) {
            module.setModuleSubtitle(subtitle);
        }
        if (!title.isBlank()) {
            module.setModuleName(title);
        }
        if (!description.isBlank()) {
            module.setModuleDescription(description);
        }

        this.repository.save(module);

        return module;
    }
}
