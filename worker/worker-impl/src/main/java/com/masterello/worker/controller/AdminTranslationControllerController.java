package com.masterello.worker.controller;

import com.masterello.auth.data.AuthZRole;
import com.masterello.commons.security.validation.AuthZRule;
import com.masterello.commons.security.validation.AuthZRules;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.dto.TranslationRequest;
import com.masterello.worker.repository.WorkerInfoRepository;
import com.masterello.worker.service.WorkerDescriptionTranslationService;
import com.masterello.worker.service.WorkerServiceDetailsTranslationService;
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

    private final WorkerDescriptionTranslationService descriptionTranslationService;
    private final WorkerServiceDetailsTranslationService serviceDetailsTranslationService;
    private final WorkerInfoRepository workerInfoRepository;

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @RequestMapping(value = "/force-update", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void storeWorkerInfo(@RequestBody @Valid TranslationRequest request) {
        workerInfoRepository.findByWorkerIdIn(request.getWorkerIds())
                .forEach(worker -> translateWorkerData(worker, request.isWithServices()));
    }

    @SneakyThrows
    private void translateWorkerData(WorkerInfo worker, boolean withServices) {
        descriptionTranslationService.translateWorkerDescription(worker.getWorkerId(), worker.getDescription());
        if (withServices) {
            translateServices(worker);
        }
        Thread.sleep(500);
    }

    private void translateServices(WorkerInfo worker) {
        worker.getServices().forEach(service ->
                serviceDetailsTranslationService.translateServiceDetails(
                        worker.getWorkerId(), service.getServiceId(), service.getDetails())
        );
    }


}
