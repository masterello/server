package com.masterello.worker.repository;

import com.masterello.user.value.Language;
import com.masterello.worker.domain.FullWorkerProjection;
import com.masterello.worker.dto.PageRequest;

import java.util.List;
import java.util.Set;
import java.util.UUID;


public interface SearchWorkerRepository {

    long getTotalCount(List<Language> languages, List<Integer> serviceIds);

    Set<UUID> findWorkersIds(List<Language> languages, List<Integer> serviceIds, int page, int size, PageRequest.Sort sort);

    List<FullWorkerProjection> findWorkers(Set<UUID> ids, PageRequest.Sort sort);
}
