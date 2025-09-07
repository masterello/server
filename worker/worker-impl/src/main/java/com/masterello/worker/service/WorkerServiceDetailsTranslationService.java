package com.masterello.worker.service;

import com.masterello.worker.domain.WorkerServiceDetailsEntity;
import com.masterello.worker.mapper.TranslationLanguageMapper;
import com.masterello.worker.repository.WorkerServiceDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerServiceDetailsTranslationService {

    private final WorkerServiceDetailsRepository workerDescriptionRepository;
    private final WorkerTranslationService workerTranslationService;
    private final TranslationLanguageMapper languageMapper;

    public void translateServiceDetails(UUID workerId, Integer serviceId, String newDetails) {
        log.info("Cleanup service details translations for worker: {}, service: {}", workerId, serviceId);
        workerDescriptionRepository.deleteAllByWorkerIdAndServiceId(workerId, serviceId);
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
            Integer serviceId,
            WorkerTranslationService.TranslatedMessages translationResponse) {
        log.info("Update description translations for worker: {}, service: {}", workerId, serviceId);
        Set<WorkerServiceDetailsEntity> allDescriptions = translationResponse.messages().stream()
                .map(translation -> new WorkerServiceDetailsEntity(
                        workerId,
                        serviceId,
                        languageMapper.translationLanguageToWorkerLanguage(translation.language()),
                        translation.message(),
                        translation.original()
                )).collect(Collectors.toSet());

        workerDescriptionRepository.saveAll(allDescriptions);
    }
}
