package com.cloudlabs.server.module;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findById(Long id);

    List<Module> findByUsers_Email(String email);

    Optional<Module> findByUsers_EmailAndId(String email, Long id);
}
