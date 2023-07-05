package com.cloudlabs.server.modules;

import com.cloudlabs.server.modules.dto.ModuleDTO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ModuleService {
    List<ModuleDTO> getAllModules();
    ModuleDTO getModuleById(Long moduleId);
    ModuleDTO addModule(ModuleDTO moduleDTO);
    ModuleDTO deleteModule(Long moduleId) throws InterruptedException, ExecutionException, TimeoutException, IOException;
    ModuleDTO updateModule(Long moduleId, ModuleDTO moduleDTO) throws InterruptedException, ExecutionException, TimeoutException, IOException;
}
