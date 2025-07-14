package com.masterello.worker.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MissingTranslationsResponse {
    List<UUID> workerIds;
}


