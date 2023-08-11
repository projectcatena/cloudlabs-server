package com.cloudlabs.server.snapshot;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SnapshotRepository extends JpaRepository<SaveSnapshot, Long> {
    SaveSnapshot findBySnapshotName(String SnapshotName);

    List<SaveSnapshot> findByUser_Email(String email);

    @Transactional
    void deleteBySnapshotName(String SnapshotName);
    }
