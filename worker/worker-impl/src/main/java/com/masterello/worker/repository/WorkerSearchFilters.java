package com.masterello.worker.repository;

import com.masterello.user.value.City;
import com.masterello.worker.domain.Language;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Builder
@Data
public class WorkerSearchFilters {
    @Nullable
    private List<Language> languages;
    @Nullable
    private List<Integer> services;
    @Nullable
    private List<City> cities;

    public boolean hasLanguageFilter() {
        return languages != null && !languages.isEmpty();
    }

    public boolean hasServiceFilter() {
        return services != null && !services.isEmpty();
    }

    public boolean hasCityFilter() {
        return cities != null && !cities.isEmpty();
    }

    public List<String> getLanguageFilter() {
        return hasLanguageFilter() ? languages.stream().map(Language::name)
                .toList() : Collections.emptyList();
    }

    public List<String> getCityFilter(){
        return hasCityFilter() ? cities.stream().map(City::getCode)
                .toList() : Collections.emptyList();
    }
}
