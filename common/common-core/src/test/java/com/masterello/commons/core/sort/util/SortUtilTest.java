package com.masterello.commons.core.sort.util;

import com.masterello.commons.core.sort.Sortable;
import com.masterello.commons.core.sort.exception.SortValidationException;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SortUtilTest {

    @Test
    void mapSortingFields() {
        val mappedFields = SortUtil.mapSortingFields(List.of("name", "workerInfo.services.amount"), FullWorker.class);
        List<String> expectedFields = List.of("name", "ws.amnt");
        assertEquals(expectedFields, mappedFields);
    }

    @Test
    void mapSortingFields_fails() {
        val sortValidationException = assertThrows(SortValidationException.class,
                () -> SortUtil.mapSortingFields(List.of("name", "lastName", "workerInfo.services.serviceId"), FullWorker.class));

        assertEquals("Fields [lastName, workerInfo.services.serviceId] are not allowed for sorting", sortValidationException.getMessage());
    }

    @Test
    void getSortableFields() {
        val sortableFields = SortUtil.getSortableFields(FullWorker.class);
        val expectedFields = Map.of("name", "name",
                "workerInfo.description", "wi.descr",
                "workerInfo.services.amount", "ws.amnt");
        assertEquals(expectedFields, sortableFields);
    }

    @Data
     static class FullWorker {

        private Integer id;
        @Sortable
        private String name;
        private String lastName;
        @Sortable(nested = true, targetTableAlias = "wi")
        private WorkerInfo workerInfo;
    }

    @Data
    static class WorkerInfo {
        @Sortable(column = "descr")
        private String description;
        @Sortable(nested = true, nestedCollectionItemType = WorkerService.class, targetTableAlias = "ws")
        private List<WorkerService> services;
    }

    @Data
    static class WorkerService {
        private String serviceId;
        @Sortable(column = "amnt")
        private int amount;
    }
}