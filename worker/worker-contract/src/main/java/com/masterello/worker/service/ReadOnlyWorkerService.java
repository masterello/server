package com.masterello.worker.service;

import com.masterello.worker.value.Worker;

import java.util.UUID;

public interface ReadOnlyWorkerService {

    Worker getWorkerInfo(UUID workerId);
}
