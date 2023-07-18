package com.cloudlabs.server.compute;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComputeRepository extends JpaRepository<Compute, Long> {
    Compute findByInstanceName(String instanceName);

    List<Compute> findByUsers_Email(String email);

    Optional<Compute> findByUsers_EmailAndInstanceName(String email,
            String instanceName);

    // Will issue select query first, then delete, so need Transactional
    // annotation
    @Transactional
    void deleteByInstanceName(String instanceName);
}
