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
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Patchable.class)) {
                editableFields.add(field.getName());
                if (Collection.class.isAssignableFrom(field.getType())) {
                    editableFields.add(field.getName() + "/.*");
                }
            }
        }
        return editableFields;
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
