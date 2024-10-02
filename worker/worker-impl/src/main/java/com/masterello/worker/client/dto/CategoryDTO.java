package com.masterello.worker.client.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryDTO(UUID uuid, String name, String description, int categoryCode, Integer parentCode,
                   boolean isService, OffsetDateTime createdDate, OffsetDateTime updatedDate, boolean active) {
}
