package com.cloudlabs.server.snapshot;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SnapshotRepository extends JpaRepository<SaveSnapshot, Long> {
    SaveSnapshot findBySnapshotName(String SnapshotName);

    @Transactional
    void deleteBySnapshotName(String SnapshotName);
    }
