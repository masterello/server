package com.masterello.worker.service;

import com.github.fge.jsonpatch.JsonPatch;
import com.masterello.category.dto.CategoryBulkRequest;
import com.masterello.category.dto.CategoryDto;
import com.masterello.category.service.ReadOnlyCategoryService;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.City;
import com.masterello.user.value.Language;
import com.masterello.user.value.MasterelloUser;
import com.masterello.worker.domain.FullWorkerPage;
import com.masterello.worker.domain.FullWorkerProjection;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.dto.PageRequestDTO;
import com.masterello.worker.exception.InvalidSearchRequestException;
import com.masterello.worker.exception.InvalidWorkerUpdateException;
import com.masterello.worker.exception.WorkerInfoNotFoundException;
import com.masterello.worker.repository.SearchWorkerRepository;
import com.masterello.worker.repository.WorkerInfoRepository;
import com.masterello.commons.core.json.service.PatchService;
import com.masterello.commons.core.sort.util.SortUtil;
import com.masterello.worker.repository.WorkerSearchFilters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerInfoRepository workerInfoRepository;
    private final PatchService patchService;
    private final SearchWorkerRepository searchWorkerRepository;
    private final ReadOnlyCategoryService categoryService;
    private final MasterelloUserService masterelloUserService;

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

    public FullWorkerPage searchWorkers(List<Language> languages, List<Integer> serviceIds, List<City> cities, PageRequestDTO pageRequestDTO) {

        final List<Integer> categoriesWithChildren = getCategories(serviceIds);
        val filters = WorkerSearchFilters.builder()
                .services(categoriesWithChildren)
                .languages(languages)
                .cities(cities).build();
        val total = searchWorkerRepository.getTotalCount(filters);
        if( total > 0 ) {
            PageRequest pageRequest = validateAndPreparePageRequest(pageRequestDTO);
            val workersIds = searchWorkerRepository.findWorkersIds(filters, pageRequest);
            return workersIds.isEmpty() ? FullWorkerPage.emptyPage(total) :
                    new FullWorkerPage(searchWorkerRepository.findWorkers(workersIds, pageRequest.getSort()), total);
        } else {
            return FullWorkerPage.emptyPage(total);
        }
    }

    public FullWorkerProjection getFullWorkerInfo(UUID workerId) {
        WorkerInfo workerInfo = getWorkerInfoOrThrow(workerId);
        MasterelloUser masterelloUser = masterelloUserService.findById(workerId)
                .orElseThrow(() -> new WorkerInfoNotFoundException("User data not found for worker"));
        return FullWorkerProjection.builder()
                .uuid(masterelloUser.getUuid())
                .name(masterelloUser.getName())
                .lastname(masterelloUser.getLastname())
                .title(masterelloUser.getTitle())
                .languages(masterelloUser.getLanguages())
                .workerInfo(workerInfo)
                .build();
    }


    @NotNull
    private List<Integer> getCategories(List<Integer> serviceIds) {
        if(serviceIds == null) {
            return Collections.emptyList();
        }
        CategoryBulkRequest categoryBulkRequest = new CategoryBulkRequest(serviceIds, true);
        Map<Integer, List<CategoryDto>> categoriesWithChildren = CollectionUtils.isEmpty(serviceIds) ? Map.of() : categoryService.getAllChildCategoriesBulk(categoryBulkRequest);
        val children = categoriesWithChildren.values().stream()
                .flatMap(List::stream)
                .map(CategoryDto::getCategoryCode);

        return Stream.concat(children, serviceIds.stream())
                .collect(Collectors.toList());
    }

    private static PageRequest validateAndPreparePageRequest(PageRequestDTO pageRequestDTO) {
        try {
            val mappedSortingFields = SortUtil.mapSortingFields(pageRequestDTO.getSort().getFields(), FullWorkerProjection.class);
            return PageRequest.of(
                    pageRequestDTO.getPage() - 1,
                    pageRequestDTO.getPageSize(),
                    pageRequestDTO.getSort().getOrder() == PageRequestDTO.SortOrder.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                    mappedSortingFields.toArray(new String[0])
            );
        } catch (Exception ex) {
            log.error("Sorting validation failed: {}", ex.getMessage());
            throw new InvalidSearchRequestException("Search failed", ex);
        }
    }
}
