package com.masterello.worker.event;

import com.masterello.translation.aspect.TranslatedFieldChangedEvent;
import com.masterello.worker.domain.WorkerInfo;

public class ServiceDetailsChangedEvent extends TranslatedFieldChangedEvent<WorkerInfo, Integer, String> {

    public ServiceDetailsChangedEvent(WorkerInfo workerInfo, Integer serviceId, String description) {
        super(workerInfo, serviceId, description);
    }
}
