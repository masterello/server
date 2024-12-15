package com.masterello.worker.dto;

import com.masterello.user.value.City;
import com.masterello.worker.domain.Language;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WorkerSearchRequest {
    @Nullable
    private List<Language> languages;
    @Nullable
    private List<Integer> services;
    @Nullable
    private List<City> cities;
    @NotNull
    @Valid
    private PageRequestDTO pageRequest;
}
