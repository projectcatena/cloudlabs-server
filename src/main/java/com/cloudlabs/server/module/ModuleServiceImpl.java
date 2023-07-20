package com.cloudlabs.server.module;

import com.cloudlabs.server.module.dto.ModuleDTO;
import com.cloudlabs.server.compute.Compute;
import com.cloudlabs.server.compute.ComputeRepository;
import com.cloudlabs.server.compute.dto.ComputeDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.management.InstanceNotFoundException;

@Service
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleRepository repository;


    @Autowired
    private ComputeRepository computeRepository;

    @Override
    public List<ModuleDTO> getAllModules() {
        List<Module> modules = repository.findAll();

        modules.sort((m1, m2) -> m1.getModuleName().compareToIgnoreCase(m2.getModuleName()));

        List<ModuleDTO> moduleDTOs = new ArrayList<ModuleDTO>();

        for (Module module : modules){
            ModuleDTO moduleDTO = new ModuleDTO();
            moduleDTO.setModuleId(module.getModuleId());
            moduleDTO.setModuleSubtitle(module.getModuleSubtitle());
            moduleDTO.setModuleName(module.getModuleName());
            moduleDTO.setModuleDescription(module.getModuleDescription());

            moduleDTOs.add(moduleDTO);
        }
        return moduleDTOs;
    }

    @Override
    public ModuleDTO getModuleById(Long moduleId) {
        Module module = repository.findByModuleId(moduleId);
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }

        // Map Module entity to ModuleDTO
        ModuleDTO moduleDTO = new ModuleDTO();
        moduleDTO.setModuleId(module.getModuleId());
        moduleDTO.setModuleSubtitle(module.getModuleSubtitle());
        moduleDTO.setModuleName(module.getModuleName());
        moduleDTO.setModuleDescription(module.getModuleDescription());

        return moduleDTO;
    }

    @Override
    public ModuleDTO addModule(ModuleDTO moduleDTO) {

        String subtitle = moduleDTO.getModuleSubtitle();
        String title = moduleDTO.getModuleName();
        String description = moduleDTO.getModuleDescription();

        if (subtitle.isBlank() || title.isBlank() || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Module details input");
        }

        Module newModule = new Module(subtitle, title, description);
        Module savedEntity = repository.save(newModule);

        ModuleDTO createdModuleDTO = new ModuleDTO();
        createdModuleDTO.setModuleId(savedEntity.getModuleId());
        createdModuleDTO.setModuleSubtitle(newModule.getModuleSubtitle());
        createdModuleDTO.setModuleName(newModule.getModuleName());
        createdModuleDTO.setModuleDescription(newModule.getModuleDescription());

        return createdModuleDTO;
    }

    @Override
    public ModuleDTO deleteModule(Long moduleId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        Module module = repository.findByModuleId(moduleId);
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }
        repository.delete(module);

        ModuleDTO moduleDTO = new ModuleDTO();
        moduleDTO.setModuleId(moduleId);

        return moduleDTO;
    }

    @Override
    public ModuleDTO updateModule(Long moduleId, ModuleDTO moduleDTO) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        Module module = repository.findByModuleId(moduleId);
        String subtitle = moduleDTO.getModuleSubtitle();
        String title = moduleDTO.getModuleName();
        String description = moduleDTO.getModuleDescription();

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

        repository.save(module);

        ModuleDTO updatedmoduleDTO = new ModuleDTO();
        updatedmoduleDTO.setModuleSubtitle(subtitle);
        updatedmoduleDTO.setModuleName(title);
        updatedmoduleDTO.setModuleDescription(description);

        return moduleDTO;
    }

    @Override
    public ModuleDTO addModuleComputeInstance(ModuleDTO moduleDTO) {
        Module module = repository.findByModuleId(moduleDTO.getModuleId());

        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }

        List<ComputeDTO> addedInstances = new ArrayList<>();
        Set<Compute> computes = new HashSet<>();

        for (ComputeDTO computeDTO : moduleDTO.getComputes()) {
            try {
                Compute compute = computeRepository.findByInstanceName(computeDTO.getInstanceName())
                        .orElseThrow(
                            () -> new InstanceNotFoundException("Virtual Machine not found."));
                computes.add(compute);
                addedInstances.add(computeDTO);
            } catch (InstanceNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
        }

        module.getComputes().addAll(computes);
        repository.save(module);

        moduleDTO.setComputes(addedInstances);

        return moduleDTO;
    }

    @Override
    public ModuleDTO removeModuleComputeInstance(ModuleDTO moduleDTO) {
        Module module = repository.findByModuleId(moduleDTO.getModuleId());

        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }

        Set<Compute> computes = module.getComputes();
        List<ComputeDTO> removedInstances = new ArrayList<>();

        for (ComputeDTO computeDTO : moduleDTO.getComputes()) {
            try {
                Compute compute = computeRepository.findByInstanceName(computeDTO.getInstanceName())
                        .orElseThrow(
                            () -> new InstanceNotFoundException("Virtual Machine not found."));
                if (computes.contains(compute)) {
                    computes.remove(compute);

                    removedInstances.add(computeDTO);
                }
            } catch (InstanceNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
        }

        module.setComputes(computes);
        repository.save(module);

        moduleDTO.setComputes(removedInstances);

        return moduleDTO;
    }
}
