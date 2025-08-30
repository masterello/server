package com.masterello.worker.repository;

import com.masterello.user.value.City;
import com.masterello.worker.domain.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface WorkerInfoCustomRepository {
    Page<UUID> findWorkerIdsByFilters(List<City> cities, List<Language> languages, List<Integer> serviceIds, boolean shouldShowTestWorkers, boolean shouldIncludeOnline, Pageable pageable);
}
