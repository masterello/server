package com.masterello.worker.repository;

import com.masterello.worker.domain.WorkerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkerInfoRepository extends JpaRepository<WorkerInfo, UUID> {

}
