package com.cloudlabs.server.module;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Module findByModuleId(Long moduleId);

    List<Module> findByUsers_Email(String email);
}
