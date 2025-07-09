package com.masterello.worker.listener;

import com.masterello.commons.async.MasterelloEventListener;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.domain.WorkerServiceDetailsEntity;
import com.masterello.worker.event.ServiceDetailsChangedEvent;
import com.masterello.worker.mapper.TranslationLanguageMapper;
import com.masterello.worker.repository.WorkerServiceDetailsRepository;
import com.masterello.worker.service.WorkerTranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkerServiceDetailsChangedEventListener extends MasterelloEventListener<ServiceDetailsChangedEvent> {


    private final WorkerServiceDetailsRepository workerDescriptionRepository;
    private final WorkerTranslationService workerTranslationService;
    private final TranslationLanguageMapper languageMapper;

    @Override
    protected void processEvent(ServiceDetailsChangedEvent event) {
        WorkerInfo workerInfo = ((WorkerInfo)event.getSource());
        UUID workerId = workerInfo.getWorkerId();
        Integer serviceId = event.getEntityId();
        log.info("Service details have changed for worker: {}, service: {}", workerId, serviceId);

        log.info("Cleanup service details translations for worker: {}, service: {}", workerId, serviceId);
        workerDescriptionRepository.deleteAllByWorkerIdAndServiceId(workerId, serviceId);
        String newDetails = event.getNewValue();
        if(StringUtils.isNotBlank(newDetails)) {
            log.info("Request new translations for worker: {}, service: {}", workerId, serviceId);
            workerTranslationService.detectLanguageAndTranslateText(
                    newDetails,
                    (translationResponse) -> storeTranslations(workerId, serviceId, translationResponse),
                    (error) -> log.error("Translation failed for worker {}, service: {}", workerId, serviceId, error)
            );
        }
    }

    private void storeTranslations(
            UUID workerId,
            Integer serviceDetails,
            WorkerTranslationService.TranslatedMessages translationResponse) {
        log.info("Update description translations for worker: {}", workerId);
        Set<WorkerServiceDetailsEntity> allDescriptions = translationResponse.messages().stream()
                .map(translation -> new WorkerServiceDetailsEntity(
                        workerId,
                        serviceDetails,
                        languageMapper.translationLanguageToWorkerLanguage(translation.language()),
                        translation.message(),
                        translation.original()
                )).collect(Collectors.toSet());

        workerDescriptionRepository.saveAll(allDescriptions);
    }
}
