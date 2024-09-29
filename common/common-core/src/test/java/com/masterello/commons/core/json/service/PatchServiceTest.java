package com.masterello.commons.core.json.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.masterello.commons.core.json.Patchable;
import com.masterello.commons.core.json.exception.PatchFailedException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class PatchServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PatchService patchService = new PatchService(objectMapper);

    @SneakyThrows
    @Test
    void applyPatch() {
        JsonNode patchNode = objectMapper.reader().readValue("[{\"op\":\"replace\",\"path\":\"/fieldB\",\"value\":\"New value B\"}," +
                "{\"op\":\"replace\",\"path\":\"/fieldC\",\"value\":\"New value C\"}, " +
                "{\"op\":\"add\",\"path\":\"/collection/-\",\"value\":\"c\"}, " +
                "{\"op\":\"add\",\"path\":\"/collection/-\",\"value\":\"d\"}]", JsonNode.class);
        JsonPatch patch = JsonPatch.fromJson(patchNode);
        TestClass testObject = new TestClass("value A", "value B", "value C", List.of("a", "b"));
        TestClass patchedObject = patchService.applyPatch(patch, testObject, TestClass.class);
        TestClass expectedObject = new TestClass("value A", "New value B", "New value C", List.of("a", "b", "c", "d"));
        assertEquals(expectedObject, patchedObject);

    }

    @SneakyThrows
    @Test
    void applyPatchError() {
        JsonNode patchNode = objectMapper.reader().readValue("[{\"op\":\"replace\",\"path\":\"/fieldA\",\"value\":\"New value A\"},{\"op\":\"replace\",\"path\":\"/fieldC\",\"value\":\"New value C\"}]", JsonNode.class);
        JsonPatch patch = JsonPatch.fromJson(patchNode);
        TestClass testObject = new TestClass("value A", "value B", "value C", null);

        PatchFailedException patchFailedException = assertThrows(PatchFailedException.class, () -> {
            patchService.applyPatch(patch, testObject, TestClass.class);
        });

        assertEquals("Fields are not supported for patching: [fieldA]", patchFailedException.getMessage());
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