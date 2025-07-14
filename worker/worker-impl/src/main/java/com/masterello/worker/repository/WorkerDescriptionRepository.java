package com.masterello.worker.repository;

import com.masterello.worker.domain.WorkerDescriptionEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface WorkerDescriptionRepository extends JpaRepository<WorkerDescriptionEntity, WorkerDescriptionEntity.WorkerDescriptionId> {

    @Transactional
    @Modifying
    void deleteAllByWorkerId(UUID workerId);

    List<WorkerDescriptionEntity> findByWorkerIdIn(Set<UUID> workerIds);

    List<WorkerDescriptionEntity> findByWorkerId(UUID workerId);

}