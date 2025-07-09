package com.masterello.worker.repository;

import com.masterello.worker.domain.WorkerInfo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface WorkerInfoRepository extends JpaRepository<WorkerInfo, UUID>, WorkerInfoCustomRepository {

    @Query("SELECT w FROM WorkerInfo w " +
            "LEFT JOIN FETCH w.languages " +
            "LEFT JOIN FETCH w.services " +
            "WHERE w.workerId IN :ids")
    List<WorkerInfo> findAllByWorkerIdIn(Set<UUID> ids, Sort sort);

    List<WorkerInfo> findByWorkerIdIn(List<UUID> ids);

}
