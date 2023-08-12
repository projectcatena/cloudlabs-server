package com.cloudlabs.server.snapshot;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SnapshotRepository extends JpaRepository<SaveSnapshot, Long> {
    //SaveSnapshot findBySnapshotName(String snapshotName);

    List<SaveSnapshot> findByUser_EmailAndInstanceName(String email, String computeName);

    Optional<SaveSnapshot> findBySnapshotNameAndInstanceName(String snapshotName, String computeName);

    @Transactional
    void deleteBySnapshotName(String SnapshotName);
    }
