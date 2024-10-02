package com.masterello.worker.domain;

import com.masterello.user.domain.Language;
import com.masterello.commons.core.sort.Sortable;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FullWorkerProjection {

    @Sortable
    private UUID uuid;
    private String title;
    @Sortable
    private String name;
    @Sortable
    private String lastname;
    @Sortable
    private String city;
    private List<Language> languages;
    @Sortable(nested = true, targetTableAlias = "wi")
    private WorkerInfo workerInfo;
}
