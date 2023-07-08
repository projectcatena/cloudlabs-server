package com.cloudlabs.server.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ModuleRepositoryTests {

    @Autowired
    private ModuleRepository moduleRepository;

    @BeforeEach
    void setup() {
        Module module = new Module("Subtitle", "Name", "Description");
        moduleRepository.save(module);
    }

    @Test
    void findModuleByModuleId() {
        Module module = new Module("Subtitle", "Name", "Description");
        moduleRepository.save(module);
        
        moduleRepository.findByModuleId(module.getModuleId());
    }

    @Test
    void deleteModuleByModuleId() {
        Module module = new Module("Subtitle", "Name", "Description");
        moduleRepository.save(module);

        moduleRepository.deleteById(module.getModuleId());
    }
    
}
