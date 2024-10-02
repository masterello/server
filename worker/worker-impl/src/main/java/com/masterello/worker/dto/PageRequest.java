package com.masterello.worker.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageRequest {
    @Min(1)
    private int page;
    private int pageSize;
    private Sort sort;

    @Data
    @Builder
    public static class Sort {
        private SortOrder order;
        private List<String> fields;
    }

    public enum SortOrder {
        ASC,
        DESC
    }
}
