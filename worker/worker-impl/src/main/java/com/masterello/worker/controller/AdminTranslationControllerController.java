package com.masterello.worker.controller;

import com.masterello.auth.data.AuthZRole;
import com.masterello.commons.security.validation.AuthZRule;
import com.masterello.commons.security.validation.AuthZRules;
import com.masterello.worker.config.WorkerConfigProperties;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.dto.MissingTranslationsResponse;
import com.masterello.worker.dto.TranslationRequest;
import com.masterello.worker.repository.WorkerInfoRepository;
import com.masterello.worker.service.WorkerDescriptionTranslationService;
import com.masterello.worker.service.WorkerServiceDetailsTranslationService;
import com.masterello.worker.service.WorkerTranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/worker/translations")
public class AdminTranslationControllerController {

    private final WorkerConfigProperties workerConfigProperties;
    private final WorkerDescriptionTranslationService descriptionTranslationService;
    private final WorkerServiceDetailsTranslationService serviceDetailsTranslationService;
    private final WorkerInfoRepository workerInfoRepository;
    private final WorkerTranslationService workerTranslationService;

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @RequestMapping(value = "/force-update", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forceUpdateTranslations(@RequestBody @Valid TranslationRequest request) {
        workerInfoRepository.findByWorkerIdIn(request.getWorkerIds())
                .forEach(worker -> translateWorkerData(worker, request.isWithDescription(), request.isWithServices()));
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @RequestMapping(value = "/missing", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MissingTranslationsResponse findWorkersWithoutTranslations() {
        // TODO implement
        return MissingTranslationsResponse.builder().build();
    }

    @SneakyThrows
    private void translateWorkerData(WorkerInfo worker, boolean withDescription, boolean withServices) {
        if(withDescription){
            descriptionTranslationService.translateWorkerDescription(worker.getWorkerId(), worker.getDescription());
        }
        if (withServices) {
            translateServices(worker);
        }
        Thread.sleep(workerConfigProperties.getBulkTranslationDelayInMillis());
    }

    private void translateServices(WorkerInfo worker) {
        worker.getServices().forEach(service ->
                serviceDetailsTranslationService.translateServiceDetails(
                        worker.getWorkerId(), service.getServiceId(), service.getDetails())
        );
    }


}
