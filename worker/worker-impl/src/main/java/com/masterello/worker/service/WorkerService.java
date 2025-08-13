package com.masterello.worker.service;

import com.github.fge.jsonpatch.JsonPatch;
import com.masterello.category.dto.CategoryBulkRequest;
import com.masterello.category.dto.CategoryDto;
import com.masterello.category.service.ReadOnlyCategoryService;
import com.masterello.commons.core.json.service.PatchService;
import com.masterello.commons.core.sort.util.SortUtil;
import com.masterello.commons.security.util.AuthContextUtil;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.City;
import com.masterello.user.value.MasterelloUser;
import com.masterello.worker.config.WorkerConfigProperties;
import com.masterello.worker.domain.FullWorkerPage;
import com.masterello.worker.domain.FullWorkerProjection;
import com.masterello.worker.domain.Language;
import com.masterello.worker.domain.TranslatedWorkerInfoProjection;
import com.masterello.worker.domain.TranslatedWorkerServiceProjection;
import com.masterello.worker.domain.WorkerDescriptionEntity;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.domain.WorkerServiceDetailsEntity;
import com.masterello.worker.domain.WorkerTranslationLanguage;
import com.masterello.worker.dto.PageRequestDTO;
import com.masterello.worker.exception.InvalidSearchRequestException;
import com.masterello.worker.exception.InvalidWorkerUpdateException;
import com.masterello.worker.exception.WorkerInfoNotFoundException;
import com.masterello.worker.exception.WorkerNotFoundException;
import com.masterello.worker.mapper.TranslatedWorkerInfoMapper;
import com.masterello.worker.mapper.TranslatedWorkerServiceDetailsMapper;
import com.masterello.worker.mapper.WorkerInfoMapper;
import com.masterello.worker.repository.WorkerDescriptionRepository;
import com.masterello.worker.repository.WorkerInfoRepository;
import com.masterello.worker.repository.WorkerServiceDetailsRepository;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolationException;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkerService implements ReadOnlyWorkerService {

    private final WorkerInfoRepository workerInfoRepository;
    private final PatchService patchService;
    private final ReadOnlyCategoryService categoryService;
    private final MasterelloUserService masterelloUserService;
    private final WorkerInfoMapper mapper;
    private final TranslatedWorkerInfoMapper translatedWorkerInfoMapper;
    private final TranslatedWorkerServiceDetailsMapper translatedWorkerServiceDetailsMapper;
    private final WorkerConfigProperties workerConfigProperties;
    private final WorkerDescriptionRepository workerDescriptionRepository;
    private final WorkerServiceDetailsRepository workerServiceDetailsRepository;

    private Pattern TEST_WORKER_EMAIL;

    @PostConstruct
    public void init() {
        this.TEST_WORKER_EMAIL = Pattern.compile(workerConfigProperties.getTestWorkerEmailPattern());
    }

    private static final PageRequestDTO.Sort DEFAULT_SORT = PageRequestDTO.Sort.builder()
            .fields(List.of("workerId"))
            .order(PageRequestDTO.SortOrder.DESC).build();

    public WorkerInfo storeWorkerInfo(WorkerInfo workerInfo) {
        MasterelloUser user = masterelloUserService.findById(workerInfo.getWorkerId())
                .orElseThrow(() -> new WorkerNotFoundException("Worker is not found for id: " + workerInfo.getWorkerId()));

        workerInfo.setActive(user.isEnabled());
        workerInfo.setVerified(user.isEmailVerified());
        workerInfo.setTest(isTestUser(user));
        return workerInfoRepository.saveAndFlush(workerInfo);
    }

    @Override
    public Optional<WorkerInfo> getWorkerInfo(UUID workerId) {
        return workerInfoRepository.findById(workerId);
    }

    public WorkerInfo updateWorkerInfo(UUID workerId, JsonPatch patch) {
        val workerInfo = getWorkerInfoOrThrow(workerId);
        WorkerInfo patchedInfo;

        try {
            patchedInfo = patchService.applyPatchWithValidation(patch, workerInfo, WorkerInfo.class, mapper::mapToDto);
        } catch (ConstraintViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidWorkerUpdateException("Error when applying update to worker info", ex);
        }
        return workerInfoRepository.saveAndFlush(patchedInfo);
    }

    private WorkerInfo getWorkerInfoOrThrow(UUID workerId) {
        return workerInfoRepository.findById(workerId)
                .orElseThrow(() -> new WorkerInfoNotFoundException("Worker info not found for worker " + workerId));
    }

    public FullWorkerPage searchWorkers(List<Language> languages, List<Integer> serviceIds, List<City> cities,
                                        boolean shouldIncludeOnline, boolean showTestWorkers, PageRequestDTO pageRequestDTO) {
        boolean shouldShowTestWorkers = showTestWorkers && AuthContextUtil.isAdmin();
        final List<Integer> categoriesWithChildren = getCategories(serviceIds);

        PageRequest pageRequest = validateAndPreparePageRequest(pageRequestDTO);
        val workerIds = workerInfoRepository.findWorkerIdsByFilters(
                cities, languages, categoriesWithChildren, shouldShowTestWorkers, shouldIncludeOnline, pageRequest);
        val workers = workerInfoRepository.findAllByWorkerIdIn(workerIds.toSet(), pageRequest.getSort());
        val fullWorkers = getFullWorkerProjections(workers);
        return new FullWorkerPage(fullWorkers, workerIds.getTotalElements());
    }

    private List<FullWorkerProjection> getFullWorkerProjections(List<WorkerInfo> workers) {
        Set<UUID> ids = workers.stream().map(WorkerInfo::getWorkerId).collect(Collectors.toSet());
        Map<UUID, MasterelloUser> users = masterelloUserService.findAllByIds(ids);
        val descriptions = workerDescriptionRepository.findByWorkerIdIn(ids).stream()
                .collect(Collectors.groupingBy(
                        WorkerDescriptionEntity::getWorkerId,
                        Collectors.toMap(WorkerDescriptionEntity::getLanguage, Function.identity())
                ));
        val details = workerServiceDetailsRepository.findByWorkerIdIn(ids).stream()
                .collect(Collectors.groupingBy(
                        WorkerServiceDetailsEntity::getWorkerId,
                        Collectors.groupingBy(WorkerServiceDetailsEntity::getServiceId,
                                Collectors.toMap(WorkerServiceDetailsEntity::getLanguage, Function.identity()))
                ));
        return workers.stream()
                .map(wi -> toFullWorkerProjection(
                                wi,
                                users.get(wi.getWorkerId()),
                                descriptions.getOrDefault(wi.getWorkerId(), Collections.emptyMap()),
                                details.getOrDefault(wi.getWorkerId(), Collections.emptyMap())
                        )
                )
                .toList();
    }

    private FullWorkerProjection toFullWorkerProjection(WorkerInfo workerInfo, MasterelloUser masterelloUser,
                                                        Map<WorkerTranslationLanguage, WorkerDescriptionEntity> descriptions,
                                                        Map<Integer, Map<WorkerTranslationLanguage, WorkerServiceDetailsEntity>> serviceDetails

    ) {
        List<TranslatedWorkerServiceProjection> translatedServiceDetails = workerInfo.getServices().stream()
                .map(s -> translatedWorkerServiceDetailsMapper.mapToTranslatedWorkerInfo(s, serviceDetails.get(s.getServiceId())))
                .collect(Collectors.toList());


        TranslatedWorkerInfoProjection translatedWorkerInfo = translatedWorkerInfoMapper
                .mapToTranslatedWorkerInfo(workerInfo, descriptions, translatedServiceDetails);
        return FullWorkerProjection.builder()
                .uuid(masterelloUser.getUuid())
                .title(masterelloUser.getTitle())
                .lastname(masterelloUser.getLastname())
                .name(masterelloUser.getName())
                .workerInfo(translatedWorkerInfo)
                .build();
    }

    private boolean isTestUser(MasterelloUser masterelloUser) {
        return TEST_WORKER_EMAIL.matcher(masterelloUser.getEmail()).matches();
    }

    public FullWorkerProjection getFullWorkerInfo(UUID workerId) {
        WorkerInfo workerInfo = getWorkerInfoOrThrow(workerId);
        MasterelloUser masterelloUser = masterelloUserService.findById(workerId)
                .orElseThrow(() -> new WorkerInfoNotFoundException("User data not found for worker"));
        Map<WorkerTranslationLanguage, WorkerDescriptionEntity> descriptions = workerDescriptionRepository.findByWorkerId(workerId).stream()
                .collect(Collectors.toMap(WorkerDescriptionEntity::getLanguage, Function.identity()));

        val serviceDetails = workerServiceDetailsRepository.findByWorkerId(workerId).stream()
                .collect(
                        Collectors.groupingBy(WorkerServiceDetailsEntity::getServiceId,
                                Collectors.toMap(WorkerServiceDetailsEntity::getLanguage, Function.identity()))
                );
        List<TranslatedWorkerServiceProjection> translatedServiceDetails = workerInfo.getServices().stream()
                .map(s -> translatedWorkerServiceDetailsMapper.mapToTranslatedWorkerInfo(s, serviceDetails.get(s.getServiceId())))
                .collect(Collectors.toList());
        return FullWorkerProjection.builder()
                .uuid(masterelloUser.getUuid())
                .name(masterelloUser.getName())
                .lastname(masterelloUser.getLastname())
                .title(masterelloUser.getTitle())
                .workerInfo(translatedWorkerInfoMapper.mapToTranslatedWorkerInfo(workerInfo, descriptions, translatedServiceDetails))
                .build();
    }


    @NotNull
    private List<Integer> getCategories(List<Integer> serviceIds) {
        if (serviceIds == null) {
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
            PageRequestDTO.Sort sort = pageRequestDTO.getSort() == null ? DEFAULT_SORT : pageRequestDTO.getSort();

            val mappedSortingFields = SortUtil.mapSortingFields(sort.getFields(), WorkerInfo.class);
            return PageRequest.of(
                    pageRequestDTO.getPage() - 1,
                    pageRequestDTO.getPageSize(),
                    sort.getOrder() == PageRequestDTO.SortOrder.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                    mappedSortingFields.toArray(new String[0])
            );
        } catch (Exception ex) {
            log.error("Sorting validation failed: {}", ex.getMessage());
            throw new InvalidSearchRequestException("Search failed", ex);
        }
    }
}
