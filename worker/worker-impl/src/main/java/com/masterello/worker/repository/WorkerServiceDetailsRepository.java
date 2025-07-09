package com.masterello.worker.repository;

import com.masterello.worker.domain.WorkerServiceDetailsEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface WorkerServiceDetailsRepository extends JpaRepository<WorkerServiceDetailsEntity, WorkerServiceDetailsEntity.WorkerServiceDetailsId> {

    @Transactional
    @Modifying
    void deleteAllByWorkerIdAndServiceId(UUID workerId, Integer serviceId);

    List<WorkerServiceDetailsEntity> findByWorkerIdIn(Set<UUID> workerIds);

    List<WorkerServiceDetailsEntity> findByWorkerId(UUID workerId);

}