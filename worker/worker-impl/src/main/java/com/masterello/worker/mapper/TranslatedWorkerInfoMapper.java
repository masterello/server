package com.masterello.worker.mapper;

import com.masterello.worker.domain.Language;
import com.masterello.worker.domain.TranslatedWorkerInfoProjection;
import com.masterello.worker.domain.TranslationLanguage;
import com.masterello.worker.domain.WorkerDescriptionEntity;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.domain.WorkerServiceEntity;
import com.masterello.worker.dto.TranslatedWorkerInfoDTO;
import com.masterello.worker.dto.WorkerDescriptionDTO;
import com.masterello.worker.dto.WorkerServiceDTO;
import lombok.val;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = {WorkerServiceMapper.class, WorkerDescriptionMapper.class})
public interface TranslatedWorkerInfoMapper {
    @Mapping(target = "descriptions", source = "descriptionEntitySet")
    TranslatedWorkerInfoProjection mapToTranslatedWorkerInfo(WorkerInfo workerInfo, Map<TranslationLanguage, WorkerDescriptionEntity> descriptionEntitySet);

    @Mappings({
            @Mapping(target = "languages", source = "languages", qualifiedByName = "setToSortedList"),
            @Mapping(target = "services", source = "services", qualifiedByName = "setToSortedServiceList"),
            @Mapping(target = "descriptions", source = "descriptions", qualifiedByName = "mapToSortedDescriptions")
    })
    TranslatedWorkerInfoDTO mapToDTO(TranslatedWorkerInfoProjection projection);

    @Named("setToSortedList")
    default List<Language> sortLanguages(Set<Language> languages) {
        if (languages != null) {
            return languages.stream()
                    .sorted(Comparator.comparing(Language::name))
                    .collect(Collectors.toList());
        }
        return null; // Return as-is if null
    }

    @Named("setToSortedServiceList")
    default List<WorkerServiceDTO> sortServices(Set<WorkerServiceEntity> services) {
        val workerServiceMapper = Mappers.getMapper(WorkerServiceMapper.class);
        if (services != null) {
            return services.stream()
                    .map(workerServiceMapper::mapToDto)
                    .sorted(Comparator.comparing(WorkerServiceDTO::getServiceId))
                    .collect(Collectors.toList());
        }
        return null; // Return as-is if null
    }

    @Named("mapToSortedDescriptions")
    default Map<TranslationLanguage, WorkerDescriptionDTO> mapToSortedDescriptions(Map<TranslationLanguage, WorkerDescriptionEntity> input) {
        val workerDescriptionMapper = Mappers.getMapper(WorkerDescriptionMapper.class);
        if (input != null) {
            return input.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(Enum::name))) // sorts by enum name
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> workerDescriptionMapper.mapToDto(entry.getValue()),
                            (a, b) -> a,
                            java.util.LinkedHashMap::new // preserve order
                    ));
        }
        return null;
    }
}
