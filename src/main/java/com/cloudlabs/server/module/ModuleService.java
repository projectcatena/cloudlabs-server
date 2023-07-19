package com.cloudlabs.server.module;

import com.cloudlabs.server.module.dto.ModuleDTO;
import com.cloudlabs.server.user.dto.UserDTO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ModuleService {
    List<ModuleDTO> getAllModules();

    ModuleDTO getModuleById(Long moduleId);

    ModuleDTO addModule(ModuleDTO moduleDTO);

    ModuleDTO deleteModule(Long moduleId) throws InterruptedException,
            ExecutionException,
            TimeoutException, IOException;

    ModuleDTO updateModule(Long moduleId, ModuleDTO moduleDTO)
            throws InterruptedException, ExecutionException, TimeoutException,
            IOException;

    ModuleDTO addUsers(ModuleDTO moduleDTO);

    ModuleDTO removeUsers(ModuleDTO moduleDTO);

    List<UserDTO> listUsers(ModuleDTO moduleDTO);
}
