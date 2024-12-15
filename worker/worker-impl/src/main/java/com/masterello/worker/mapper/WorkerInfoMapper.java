package com.masterello.worker.mapper;

import com.masterello.worker.domain.Language;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.dto.WorkerInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
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

    @Mapping(target = "languages", source = "languages", qualifiedByName = "setToSortedList")
    WorkerInfoDTO mapToDto(WorkerInfo workerInfo);

    @Mappings(value = {
            @Mapping(target = "workerId", ignore = true),
            @Mapping(target = "languages", source = "languages", qualifiedByName = "listToSet")}
    )
    WorkerInfo mapToEntity(WorkerInfoDTO workerInfoDTO);

    @Named("setToSortedList")
    default List<Language> sortLanguages(Set<Language> languages) {
        if (languages != null) {
            return languages.stream()
                    .sorted(Comparator.comparing(Language::name))
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
}
