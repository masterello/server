package com.masterello.worker.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TranslationRequest {
    List<UUID> workerIds;
    boolean withDescription;
    boolean withServices;
}
