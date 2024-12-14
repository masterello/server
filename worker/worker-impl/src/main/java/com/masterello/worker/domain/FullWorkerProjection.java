package com.masterello.worker.domain;

import com.masterello.commons.core.sort.Sortable;
import com.masterello.user.value.Language;
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
    private List<Language> languages;
    @Sortable(nested = true, targetTableAlias = "wi")
    private WorkerInfo workerInfo;
}
