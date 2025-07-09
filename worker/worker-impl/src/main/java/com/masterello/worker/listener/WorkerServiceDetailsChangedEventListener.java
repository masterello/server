package com.masterello.worker.listener;

import com.masterello.commons.async.MasterelloEventListener;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.event.ServiceDetailsChangedEvent;
import com.masterello.worker.service.WorkerServiceDetailsTranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkerServiceDetailsChangedEventListener extends MasterelloEventListener<ServiceDetailsChangedEvent> {


    private final WorkerServiceDetailsTranslationService workerServiceDetailsTranslationService;

    @Override
    protected void processEvent(ServiceDetailsChangedEvent event) {
        WorkerInfo workerInfo = ((WorkerInfo) event.getSource());
        UUID workerId = workerInfo.getWorkerId();
        Integer serviceId = event.getEntityId();
        log.info("Service details have changed for worker: {}, service: {}", workerId, serviceId);

        workerServiceDetailsTranslationService.translateServiceDetails(workerId, serviceId, event.getNewValue());
    }
}
