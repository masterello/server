package com.masterello.worker.controller;

import com.github.fge.jsonpatch.JsonPatch;
import com.masterello.auth.data.AuthZRole;
import com.masterello.commons.security.validation.AuthZRule;
import com.masterello.commons.security.validation.AuthZRules;
import com.masterello.commons.security.validation.OwnerId;
import com.masterello.worker.domain.FullWorkerPage;
import com.masterello.worker.domain.FullWorkerProjection;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.dto.FullWorkerDTO;
import com.masterello.worker.dto.WorkerInfoDTO;
import com.masterello.worker.dto.WorkerSearchRequest;
import com.masterello.worker.dto.WorkerSearchResponse;
import com.masterello.worker.dto.WorkerServiceDTO;
import com.masterello.worker.exception.WorkerInfoNotFoundException;
import com.masterello.worker.mapper.FullWorkerMapper;
import com.masterello.worker.mapper.WorkerInfoMapper;
import com.masterello.worker.service.WorkerService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    private final WorkerService workerService;
    private final WorkerInfoMapper workerInfoMapper;
    private final FullWorkerMapper fullWorkerMapper;

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.WORKER}, isOwner = true),
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @RequestMapping(value = "/{worker_uuid}/info", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public WorkerInfoDTO<WorkerServiceDTO> storeWorkerInfo(@OwnerId @PathVariable("worker_uuid") @Parameter(required = true) UUID workerId,
                                                           @RequestBody @Valid WorkerInfoDTO<WorkerServiceDTO> request) {
        WorkerInfo infoToStore = workerInfoMapper.mapToEntity(request);
        infoToStore.setWorkerId(workerId);
        val stored = workerService.storeWorkerInfo(infoToStore);
        return workerInfoMapper.mapToDto(stored);
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.WORKER}, isOwner = true),
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @RequestMapping(value = "/{worker_uuid}/info", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public WorkerInfoDTO<WorkerServiceDTO> getWorkerInfo(@OwnerId @PathVariable("worker_uuid") @Parameter(required = true) UUID workerId) {
        val workerInfo = workerService.getWorkerInfo(workerId);
        return workerInfo
                .map(workerInfoMapper::mapToDto)
                .orElseThrow(() -> new WorkerInfoNotFoundException("Worker info not found for worker " + workerId));
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.WORKER}, isOwner = true),
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @PatchMapping(value = "/{worker_uuid}/info", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {"application/json-patch+json"})
    public WorkerInfoDTO<WorkerServiceDTO> patchWorkerInfo(@OwnerId @PathVariable("worker_uuid") UUID workerId, @RequestBody JsonPatch patch) {
        WorkerInfo workerInfo = workerService.updateWorkerInfo(workerId, patch);
        return workerInfoMapper.mapToDto(workerInfo);
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public WorkerSearchResponse searchWorkers(@Valid @RequestBody WorkerSearchRequest request) {
        FullWorkerPage page = workerService.searchWorkers(
                request.getLanguages(),
                request.getServices(),
                request.getCities(),
                request.isIncludeOnline(),
                request.isShowTestWorkers(),
                request.getPageRequest()
        );
        List<FullWorkerDTO> workers = page.items().stream()
                .map(fullWorkerMapper::mapToDto)
                .toList();
        return new WorkerSearchResponse(workers, page.total());
    }

    @GetMapping(value = "/{worker_uuid}/full-info")
    @ResponseStatus(HttpStatus.OK)
    public FullWorkerDTO getWorkerFullInfo(@PathVariable("worker_uuid") @Parameter(required = true) UUID workerId) {
        FullWorkerProjection fullWorkerInfo = workerService.getFullWorkerInfo(workerId);
        return fullWorkerMapper.mapToDto(fullWorkerInfo);
    }
}
