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
import com.masterello.worker.domain.WorkerDescriptionEntity;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.domain.WorkerTranslationLanguage;
import com.masterello.worker.dto.PageRequestDTO;
import com.masterello.worker.exception.InvalidSearchRequestException;
import com.masterello.worker.exception.InvalidWorkerUpdateException;
import com.masterello.worker.exception.WorkerInfoNotFoundException;
import com.masterello.worker.exception.WorkerNotFoundException;
import com.masterello.worker.mapper.TranslatedWorkerInfoMapper;
import com.masterello.worker.mapper.TranslationLanguageMapper;
import com.masterello.worker.mapper.WorkerInfoMapper;
import com.masterello.worker.repository.WorkerDescriptionRepository;
import com.masterello.worker.repository.WorkerInfoRepository;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final WorkerConfigProperties workerConfigProperties;
    private final WorkerTranslationService workerTranslationService;
    private final WorkerDescriptionRepository workerDescriptionRepository;
    private final TranslationLanguageMapper languageMapper;

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
        updateDescriptionTranslationIfNeeded(workerInfo.getWorkerId(), workerInfo.getDescription());
        return workerInfoRepository.saveAndFlush(workerInfo);
    }

    private void updateDescriptionTranslationIfNeeded(UUID workerId, String newDescription) {
        if (hasDescriptionChanged(workerId, newDescription)) {
            log.info("Description has changed for worker: {}", workerId);
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


    private boolean hasDescriptionChanged(UUID workerId, String newDescription) {
        String existingDescription = workerInfoRepository.findById(workerId)
                .map(WorkerInfo::getDescription)
                .orElse(null);
        return !Objects.equals(existingDescription, newDescription);
    }

    @Override
    public Optional<WorkerInfo> getWorkerInfo(UUID workerId) {
        return workerInfoRepository.findById(workerId);
    }

    public WorkerInfo updateWorkerInfo(UUID workerId, JsonPatch patch) {
        val workerInfo = getWorkerInfoOrThrow(workerId);
        WorkerInfo patcherInfo;

        try {
            patcherInfo = patchService.applyPatchWithValidation(patch, workerInfo, WorkerInfo.class, mapper::mapToDto);
        } catch (ConstraintViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidWorkerUpdateException("Error when applying update to worker info", ex);
        }
        updateDescriptionTranslationIfNeeded(workerInfo.getWorkerId(), patcherInfo.getDescription());
        return workerInfoRepository.saveAndFlush(patcherInfo);
    }

    private WorkerInfo getWorkerInfoOrThrow(UUID workerId) {
        return workerInfoRepository.findById(workerId)
                .orElseThrow(() -> new WorkerInfoNotFoundException("Worker info not found for worker " + workerId));
    }

    public FullWorkerPage searchWorkers(List<Language> languages, List<Integer> serviceIds, List<City> cities,
                                        PageRequestDTO pageRequestDTO, boolean showTestWorkers) {
        boolean shouldShowTestWorkers = showTestWorkers && AuthContextUtil.isAdmin();
        final List<Integer> categoriesWithChildren = getCategories(serviceIds);

        PageRequest pageRequest = validateAndPreparePageRequest(pageRequestDTO);
        val workerIds = workerInfoRepository.findWorkerIdsByFilters(cities, languages, categoriesWithChildren, shouldShowTestWorkers, pageRequest);
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
        return workers.stream()
                .map(wi -> toFullWorkerProjection(wi, users.get(wi.getWorkerId()), descriptions))
                .toList();
    }

    private FullWorkerProjection toFullWorkerProjection(WorkerInfo workerInfo, MasterelloUser masterelloUser,
                                                        Map<UUID, Map<WorkerTranslationLanguage, WorkerDescriptionEntity>> descriptions
    ) {
        return FullWorkerProjection.builder()
                .uuid(masterelloUser.getUuid())
                .title(masterelloUser.getTitle())
                .lastname(masterelloUser.getLastname())
                .name(masterelloUser.getName())
                .workerInfo(translatedWorkerInfoMapper.mapToTranslatedWorkerInfo(workerInfo, descriptions.get(workerInfo.getWorkerId())))
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
        return FullWorkerProjection.builder()
                .uuid(masterelloUser.getUuid())
                .name(masterelloUser.getName())
                .lastname(masterelloUser.getLastname())
                .title(masterelloUser.getTitle())
                .workerInfo(translatedWorkerInfoMapper.mapToTranslatedWorkerInfo(workerInfo, descriptions))
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
