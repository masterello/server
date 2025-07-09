package com.masterello.worker.service;

import com.masterello.worker.domain.WorkerDescriptionEntity;
import com.masterello.worker.mapper.TranslationLanguageMapper;
import com.masterello.worker.repository.WorkerDescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerDescriptionTranslationService {

    private final WorkerDescriptionRepository workerDescriptionRepository;
    private final WorkerTranslationService workerTranslationService;
    private final TranslationLanguageMapper languageMapper;

    @Async
    public void translateWorkerDescription(UUID workerId, String newDescription) {
        log.info("Cleanup all translations for worker: {}", workerId);
        workerDescriptionRepository.deleteAllByWorkerId(workerId);

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
