package com.masterello.worker.service;

import com.github.fge.jsonpatch.JsonPatch;
import com.masterello.user.domain.Language;
import com.masterello.worker.client.CategoryServiceClient;
import com.masterello.worker.domain.FullWorkerPage;
import com.masterello.worker.domain.FullWorkerProjection;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.dto.PageRequest;
import com.masterello.worker.exception.InvalidSearchRequestException;
import com.masterello.worker.exception.InvalidWorkerUpdateException;
import com.masterello.worker.exception.WorkerInfoNotFoundException;
import com.masterello.worker.repository.SearchWorkerRepository;
import com.masterello.worker.repository.WorkerInfoRepository;
import com.masterello.commons.core.json.service.PatchService;
import com.masterello.commons.core.sort.util.SortUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerInfoRepository workerInfoRepository;
    private final PatchService patchService;
    private final SearchWorkerRepository searchWorkerRepository;
    private final CategoryServiceClient categoryServiceClient;

    public WorkerInfo storeWorkerInfo(WorkerInfo workerInfo) {
        return workerInfoRepository.save(workerInfo);
    }

    public WorkerInfo getWorkerInfo(UUID workerId) {
        return getWorkerInfoOrThrow(workerId);
    }

    public WorkerInfo updateWorkerInfo(UUID workerId, JsonPatch patch) {
        val workerInfo = getWorkerInfoOrThrow(workerId);
        WorkerInfo patcherInfo;

        try {
            patcherInfo = patchService.applyPatch(patch, workerInfo, WorkerInfo.class);
        } catch (Exception ex) {
            throw new InvalidWorkerUpdateException("Error when applying update to worker info", ex);
        }
        return workerInfoRepository.saveAndFlush(patcherInfo);
    }

    private WorkerInfo getWorkerInfoOrThrow(UUID workerId) {
        return workerInfoRepository.findById(workerId)
                .orElseThrow(() -> new WorkerInfoNotFoundException("Worker info not found for worker " + workerId));
    }

    public FullWorkerPage searchWorkers(List<Language> languages, List<Integer> serviceIds, PageRequest pageRequest) {
        val categoriesWithChildren = CollectionUtils.isEmpty(serviceIds) ? serviceIds : categoryServiceClient.getWithChildCategoryCodes(serviceIds);
        PageRequest.Sort sort = validateAndGetSort(pageRequest);
        val total = searchWorkerRepository.getTotalCount(languages, categoriesWithChildren);
        if( total > 0 ) {
            int page = pageRequest.getPage() - 1;
            val workersIds = searchWorkerRepository.findWorkersIds(languages, categoriesWithChildren,
                    page, pageRequest.getPageSize(), sort);
            return workersIds.isEmpty() ? FullWorkerPage.emptyPage(total) :
                    new FullWorkerPage(searchWorkerRepository.findWorkers(workersIds, sort), total);
        } else {
            return FullWorkerPage.emptyPage(total);
        }
    }

    private static PageRequest.Sort validateAndGetSort(PageRequest pageRequest) {
        try {
            val mappedSortingFields = SortUtil.mapSortingFields(pageRequest.getSort().getFields(), FullWorkerProjection.class);
            return PageRequest.Sort.builder()
                    .fields(mappedSortingFields)
                    .order(pageRequest.getSort().getOrder())
                    .build();
        } catch (Exception ex) {
            log.error("Sorting validation failed: {}", ex.getMessage());
            throw new InvalidSearchRequestException("Search failed", ex);
        }
    }
}
