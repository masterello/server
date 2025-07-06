package com.masterello.worker.listener;

import com.masterello.commons.async.MasterelloEventListener;
import com.masterello.worker.domain.WorkerDescriptionEntity;
import com.masterello.worker.event.DescriptionChangedEvent;
import com.masterello.worker.mapper.TranslationLanguageMapper;
import com.masterello.worker.repository.WorkerDescriptionRepository;
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
public class WorkerDescriptionChangedEventListener extends MasterelloEventListener<DescriptionChangedEvent> {


    private final WorkerDescriptionRepository workerDescriptionRepository;
    private final WorkerTranslationService workerTranslationService;
    private final TranslationLanguageMapper languageMapper;

    @Override
    protected void processEvent(DescriptionChangedEvent event) {
        UUID workerId = event.getEntityId();
        log.info("Description has changed for worker: {}", workerId);
        log.info("Cleanup all translations for worker: {}", workerId);
        workerDescriptionRepository.deleteAllByWorkerId(workerId);

        String newDescription = event.getNewValue();
        if (StringUtils.isNotBlank(newDescription)) {
            log.info("Request translations for worker: {}", workerId);
            workerTranslationService.detectLanguageAndTranslateText(
                    newDescription,
                    (translationResponse) -> storeTranslations(workerId, translationResponse),
                    (error) -> log.error("Translation failed for worker {}", workerId, error)
            );
        }
    }

    private void storeTranslations(
            UUID workerId,
            WorkerTranslationService.TranslatedMessages translationResponse) {
        log.info("Update description translations for worker: {}", workerId);
        Set<WorkerDescriptionEntity> allDescriptions = translationResponse.messages().stream()
                .map(translation -> new WorkerDescriptionEntity(
                        workerId,
                        languageMapper.translationLanguageToWorkerLanguage(translation.language()),
                        translation.message(),
                        translation.original()
                )).collect(Collectors.toSet());

        workerDescriptionRepository.saveAll(allDescriptions);
    }

}
