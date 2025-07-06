package com.masterello.worker.event;

import com.masterello.translation.aspect.TranslatedFieldChangedEvent;
import com.masterello.worker.domain.WorkerInfo;

import java.util.UUID;

public class DescriptionChangedEvent extends TranslatedFieldChangedEvent<WorkerInfo, UUID, String> {

    public DescriptionChangedEvent(WorkerInfo workerInfo, UUID entityId, String description) {
        super(workerInfo, entityId, description);
    }
}
