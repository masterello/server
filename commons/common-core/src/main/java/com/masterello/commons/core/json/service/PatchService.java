package com.masterello.commons.core.json.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.masterello.commons.core.json.exception.PatchFailedException;
import com.masterello.commons.core.json.util.PatchUtil;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PatchService {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public <T> T applyPatch(JsonPatch patch, T object, Class<T> clazz) {
        try {
            JsonNode sourceObject = objectMapper.convertValue(object, JsonNode.class);
            JsonNode patched = patch.apply(sourceObject);
            Set<String> editableFields = PatchUtil.getEditableFields(clazz);
            Set<String> editedFields =  PatchUtil.getUpdatedFields(sourceObject, patched);
            Collection<String> forbiddenFields = new ArrayList<>();
            editedFields.forEach(editedField -> {
                if(editableFields.stream().noneMatch(editedField::matches)) {
                    forbiddenFields.add(editedField);
                }
            });
            if(!forbiddenFields.isEmpty()) {
                throw new PatchFailedException("Fields are not supported for patching: " + forbiddenFields);
            }
            return objectMapper.treeToValue(patched, clazz);
        } catch (JsonPatchException | JsonProcessingException e) {
            throw new PatchFailedException("patch failed", e);
        }
    }

    public <T, D> T applyPatchWithValidation(JsonPatch patch, T object, Class<T> clazz,
                                             Class<D> validatingClass, Function<T, D> mapper) {
        T patchedEntity = applyPatch(patch, object, clazz);
        D objectToValidate = mapper.apply(patchedEntity);
        val violations = validator.validate(objectToValidate);

        if (!violations.isEmpty()) {
            // Just throw it like Spring does internally
            throw new ConstraintViolationException(violations);
        }
        return patchedEntity;
    }
}