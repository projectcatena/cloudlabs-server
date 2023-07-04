package com.cloudlabs.server.modules;

import java.util.List;

public interface ModuleService {
    List<Module> getAllModules();
    Module getModuleById(Long moduleId);
    Module addModule(String subtitle, String title, String description);
    void deleteModule(Long moduleId);
    Module updateModule(Long moduleId, String subtitle, String title, String description);
}
