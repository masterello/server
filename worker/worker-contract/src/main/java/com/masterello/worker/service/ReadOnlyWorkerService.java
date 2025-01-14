package com.masterello.worker.service;

import com.masterello.worker.value.Worker;

import java.util.Optional;
import java.util.UUID;

public interface ReadOnlyWorkerService {

    Optional<? extends Worker> getWorkerInfo(UUID workerId);
}
