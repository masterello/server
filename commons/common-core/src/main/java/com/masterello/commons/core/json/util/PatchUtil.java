package com.masterello.commons.core.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.masterello.commons.core.json.Patchable;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class PatchUtil {

    public <T> Set<String> getEditableFields(Class<T> clazz) {
        Set<String> editableFields = new HashSet<>();
        Set<Class<?>> visited = new HashSet<>();
        collectEditableFields(clazz, "", editableFields, visited);
        return editableFields;
    }

    private void collectEditableFields(Class<?> clazz, String prefix, Set<String> out, Set<Class<?>> visited) {
        if (clazz == null || isJdkType(clazz)) {
            return;
        }
        if (!visited.add(clazz)) {
            return; // prevent cycles
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Patchable.class)) {
                continue;
            }
            String path = prefix + field.getName();
            out.add(path);
            Class<?> type = field.getType();
            if (Collection.class.isAssignableFrom(type) || type.isArray()) {
                out.add(path + "/.*");
            }
            // Recurse into nested complex types to support nested patchable fields
            if (!Collection.class.isAssignableFrom(type)
                    && !type.isArray()
                    && !isJdkType(type)
                    && !type.isEnum()) {
                collectEditableFields(type, path + "/", out, visited);
            }
        }
    }

    private boolean isJdkType(Class<?> type) {
        return type.isPrimitive()
                || type.getName().startsWith("java.")
                || type.getName().startsWith("jakarta.")
                || type.getName().startsWith("javax.");
    }

    public Set<String> getUpdatedFields(JsonNode source, JsonNode target) {
        JsonNode diffNode = JsonDiff.asJson(source, target);
        Set<String> fields = new HashSet<>();
        if (diffNode.isArray()) {
            for (JsonNode jsonNode : diffNode) {
                JsonNode pathFieldNode = jsonNode.get("path");
                fields.add(pathFieldNode.asText().substring(1)); // Removing slash
            }
        }
        return fields;
    }

}
