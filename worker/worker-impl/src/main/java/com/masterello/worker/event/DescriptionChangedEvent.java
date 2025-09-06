package com.masterello.worker.event;

import com.masterello.commons.data.change.event.FieldChangedEvent;
import com.masterello.worker.domain.WorkerInfo;

import java.util.UUID;

public class DescriptionChangedEvent extends FieldChangedEvent<WorkerInfo, UUID, String> {

    public DescriptionChangedEvent(WorkerInfo workerInfo, UUID entityId, String description) {
        super(workerInfo, entityId, description);
    }
}
