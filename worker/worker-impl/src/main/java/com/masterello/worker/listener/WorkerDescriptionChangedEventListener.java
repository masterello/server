package com.masterello.worker.listener;

import com.masterello.commons.async.MasterelloEventListener;
import com.masterello.worker.event.DescriptionChangedEvent;
import com.masterello.worker.service.WorkerDescriptionTranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkerDescriptionChangedEventListener extends MasterelloEventListener<DescriptionChangedEvent> {


    private final WorkerDescriptionTranslationService workerDescriptionTranslationService;

    @Override
    protected void processEvent(DescriptionChangedEvent event) {
        UUID workerId = event.getEntityId();
        log.info("Description has changed for worker: {}", workerId);
        workerDescriptionTranslationService.translateWorkerDescription(workerId, event.getNewValue());
    }
}
