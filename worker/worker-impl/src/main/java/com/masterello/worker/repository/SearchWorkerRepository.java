package com.masterello.worker.repository;

import com.masterello.worker.domain.FullWorkerProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;
import java.util.UUID;


public interface SearchWorkerRepository {

    long getTotalCount(WorkerSearchFilters filters);

    Set<UUID> findWorkersIds(WorkerSearchFilters filters, PageRequest page);

    List<FullWorkerProjection> findWorkers(Set<UUID> ids, Sort sort);
}
