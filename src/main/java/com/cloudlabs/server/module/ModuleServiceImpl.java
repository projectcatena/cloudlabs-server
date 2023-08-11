package com.cloudlabs.server.module;

import com.cloudlabs.server.compute.Compute;
import com.cloudlabs.server.compute.ComputeRepository;
import com.cloudlabs.server.compute.dto.ComputeDTO;
import com.cloudlabs.server.module.dto.ModuleDTO;
import com.cloudlabs.server.role.dto.RoleDTO;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserRepository;
import com.cloudlabs.server.user.dto.UserDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.management.InstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleRepository repository;

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<ModuleDTO> getAllModules() {
        List<Module> modules = repository.findAll();

        modules.sort(
                (m1, m2) -> m1.getModuleName().compareToIgnoreCase(m2.getModuleName()));

        List<ModuleDTO> moduleDTOs = new ArrayList<ModuleDTO>();

        for (Module module : modules) {
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
    public List<ModuleDTO> getUserModules() {

        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authenticationToken.getName();

        List<Module> modules = repository.findByUsers_Email(email);

        List<ModuleDTO> moduleDTOs = new ArrayList<>();

        for (Module module : modules) {
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
        Module module = repository.findById(moduleId).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));;

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid Module details input");
        }

        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                    .getContext()
                    .getAuthentication();

        String email = authenticationToken.getName();

        User user = userRepository.findByEmail(email).orElseThrow(
            () -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unrecoverable error, user not found"));
        
        Set<User> users = new HashSet<User>();
        users.add(user);

        Module newModule = new Module(subtitle, title, description);
        newModule.setUsers(users);
        Module savedEntity = repository.save(newModule);

        ModuleDTO createdModuleDTO = new ModuleDTO();
        createdModuleDTO.setModuleId(savedEntity.getModuleId());
        createdModuleDTO.setModuleSubtitle(newModule.getModuleSubtitle());
        createdModuleDTO.setModuleName(newModule.getModuleName());
        createdModuleDTO.setModuleDescription(newModule.getModuleDescription());

        return createdModuleDTO;
    }

    @Override
    public ModuleDTO deleteModule(Long moduleId)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException {
        Module module = repository.findById(moduleId).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));;

        repository.delete(module);

        ModuleDTO moduleDTO = new ModuleDTO();
        moduleDTO.setModuleId(moduleId);

        return moduleDTO;
    }

    @Override
    public ModuleDTO updateModule(Long moduleId, ModuleDTO moduleDTO)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException {
        Module module = repository.findById(moduleId).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));;
        String subtitle = moduleDTO.getModuleSubtitle();
        String title = moduleDTO.getModuleName();
        String description = moduleDTO.getModuleDescription();

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
        Module module = repository.findById(moduleDTO.getModuleId()).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));;

        List<ComputeDTO> addedInstances = new ArrayList<>();
        Set<Compute> computes = new HashSet<>();

        for (ComputeDTO computeDTO : moduleDTO.getComputes()) {
            try {
                Compute compute = computeRepository.findByInstanceName(computeDTO.getInstanceName())
                        .orElseThrow(() -> new InstanceNotFoundException(
                                "Virtual Machine not found."));
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

    public ModuleDTO addUsers(ModuleDTO moduleDTO) {
        Module module = repository.findById(moduleDTO.getModuleId()).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));;

        List<UserDTO> addedUsers = new ArrayList<>();
        Set<User> users = new HashSet<>();

        for (UserDTO userDTO : moduleDTO.getUsers()) {
            User user = userRepository.findByEmail(userDTO.getEmail())
                    .orElseThrow(
                            () -> new UsernameNotFoundException("User not found!"));
            users.add(user);
            addedUsers.add(userDTO);
        }

        module.getUsers().addAll(users);
        repository.save(module);

        moduleDTO.setUsers(addedUsers);

        return moduleDTO;
    }

    @Override
    public ModuleDTO removeModuleComputeInstance(ModuleDTO moduleDTO) {
        Module module = repository.findById(moduleDTO.getModuleId()).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));;

        Set<Compute> computes = module.getComputes();
        List<ComputeDTO> removedInstances = new ArrayList<>();

        for (ComputeDTO computeDTO : moduleDTO.getComputes()) {
            try {
                Compute compute = computeRepository.findByInstanceName(computeDTO.getInstanceName())
                        .orElseThrow(() -> new InstanceNotFoundException(
                                "Virtual Machine not found."));
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

    public ModuleDTO removeUsers(ModuleDTO moduleDTO) {

        Module module = repository.findById(moduleDTO.getModuleId()).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));;

        // Get list of users from entity
        Set<User> users = module.getUsers();

        List<UserDTO> removedUsers = new ArrayList<>();

        // Get a list of users to remove
        for (UserDTO userDTO : moduleDTO.getUsers()) {
            // Ensure valid user
            User user = userRepository.findByEmail(userDTO.getEmail())
                    .orElseThrow(
                            () -> new UsernameNotFoundException("User not found!"));

            // If is in list of assigned users, consider valid
            if (users.contains(user)) {
                users.remove(user);

                // Add to list of users removed
                removedUsers.add(userDTO);
            }
        }

        // Flush changes to database
        module.setUsers(users);
        repository.save(module);

        moduleDTO.setUsers(removedUsers);

        return moduleDTO;
    }

    /*
     * Useful for tutors to get a list of students assigned to a specific module
     */
    @Override
    public List<UserDTO> listUsers(ModuleDTO moduleDTO) {
        Module module = repository.findById(moduleDTO.getModuleId()).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));;

        List<UserDTO> userDTOs = module.getUsers()
                .stream()
                .map(user -> new UserDTO(user.getFullname(), user.getUserName(),
                        user.getEmail(), null,
                        user.getRoles()
                                .stream()
                                .map(role -> new RoleDTO(role.getName()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        return userDTOs;
    }

    @Override
    public List<UserDTO> listUsersWithModules() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userList = new ArrayList<UserDTO>();
        for (User user: users) {
            List<ModuleDTO> moduleDTOs = new ArrayList<ModuleDTO>();
            List<Module> modules = repository.findByUsers_Email(user.getEmail());
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(user.getEmail());
            userDTO.setFullname(user.getFullname());
            userDTO.setUsername(user.getUserName());
            for (Module module: modules) {
                ModuleDTO moduleDTO = new ModuleDTO();
                moduleDTO.setModuleName(module.getModuleName());
                moduleDTOs.add(moduleDTO);
            }
            userDTO.setModules(moduleDTOs);
            userList.add(userDTO);
        }
        
        return userList;
    }
}
