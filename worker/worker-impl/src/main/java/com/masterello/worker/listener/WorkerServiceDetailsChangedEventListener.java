package com.masterello.worker.listener;

import com.masterello.commons.async.MasterelloEventListener;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.event.ServiceDetailsChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkerServiceDetailsChangedEventListener extends MasterelloEventListener<ServiceDetailsChangedEvent> {


//    private final WorkerDescriptionRepository workerDescriptionRepository;
//    private final WorkerTranslationService workerTranslationService;
//    private final TranslationLanguageMapper languageMapper;

    @Override
    protected void processEvent(ServiceDetailsChangedEvent event) {
        WorkerInfo workerInfo = ((WorkerInfo)event.getSource());
        UUID workerId = workerInfo.getWorkerId();
        Integer serviceId = event.getEntityId();
        log.info("Details have changed for worker: {}, service: {}", workerId, serviceId);

        log.info("Cleanup all translations for worker: {}, service: {}", workerId, serviceId);

        if(StringUtils.isNotBlank(event.getNewValue())) {
            log.info("Request new translations for worker: {}, service: {}", workerId, serviceId);
        }
    }

}
