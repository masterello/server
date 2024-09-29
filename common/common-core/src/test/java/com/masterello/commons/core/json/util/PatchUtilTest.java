package com.masterello.commons.core.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.commons.core.json.Patchable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PatchUtilTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getEditableFields() {
        Set<String> editableFields = PatchUtil.getEditableFields(TestClass.class);
        List<String> expectedFields = List.of("fieldB", "fieldC", "collection", "collection/.*");
        assertTrue(editableFields.containsAll(expectedFields) && expectedFields.containsAll(editableFields));

    }

    @Test
    void getUpdatedFields() {
        TestClass testObject = new TestClass("value A", "value B", "value C", null);
        JsonNode source = objectMapper.convertValue(testObject, JsonNode.class);
        testObject.setFieldB("New value B");
        JsonNode target = objectMapper.convertValue(testObject, JsonNode.class);

        Set<String> updatedFields = PatchUtil.getUpdatedFields(source, target);
        List<String> expectedFields = List.of("fieldB");

        assertTrue(updatedFields.containsAll(expectedFields) && expectedFields.containsAll(updatedFields));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestClass {
        String fieldA;
        @Patchable
        String fieldB;
        @Patchable
        String fieldC;
        @Patchable
        List<String> collection;
    }
}