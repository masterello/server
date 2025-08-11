package com.masterello.worker.mapper;

import com.masterello.worker.domain.Language;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.domain.WorkerServiceEntity;
import com.masterello.worker.dto.WorkerInfoDTO;
import com.masterello.worker.dto.WorkerServiceDTO;
import lombok.val;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Qualifier("workerInfoMapper")
@Mapper(componentModel = "spring",
        uses = WorkerServiceMapper.class)
public interface WorkerInfoMapper {


    @Mappings({
            @Mapping(target = "languages", source = "languages", qualifiedByName = "setToSortedList"),
            @Mapping(target = "services", source = "services", qualifiedByName = "setToSortedServiceList")
    })
    WorkerInfoDTO<WorkerServiceDTO> mapToDto(WorkerInfo workerInfo);


    @Mappings({
            @Mapping(target = "workerId", ignore = true),
            @Mapping(target = "active", ignore = true),
            @Mapping(target = "verified", ignore = true),
            @Mapping(target = "languages", source = "languages", qualifiedByName = "listToSet"),
            @Mapping(target = "services", source = "services", qualifiedByName = "listToSetService")
    })
    WorkerInfo mapToEntity(WorkerInfoDTO<WorkerServiceDTO> workerInfoDTO);

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

    @Named("listToSet")
    default Set<Language> listToSet(List<Language> languages) {
        if (languages == null) {
            return null;
        }
        return new HashSet<>(languages);
    }

    @Named("listToSetService")
    default Set<WorkerServiceEntity> listToSetService(List<WorkerServiceDTO> services) {
        if (services == null) {
            return null;
        }
        val workerServiceMapper = Mappers.getMapper(WorkerServiceMapper.class);
        return services.stream()
                .map(workerServiceMapper::mapToEntity)
                .collect(Collectors.toSet());
    }
}
