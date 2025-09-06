package com.masterello.worker.event;

import com.masterello.commons.data.change.event.FieldChangedEvent;
import com.masterello.worker.domain.WorkerInfo;

public class ServiceDetailsChangedEvent extends FieldChangedEvent<WorkerInfo, Integer, String> {

    public ServiceDetailsChangedEvent(WorkerInfo workerInfo, Integer serviceId, String description) {
        super(workerInfo, serviceId, description);
    }
}
