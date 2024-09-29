package com.masterello.commons.core.sort.util;

import com.masterello.commons.core.sort.Sortable;
import com.masterello.commons.core.sort.exception.SortValidationException;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class SortUtil {

    /**
     * Validates that items of sortFields are allowed for sorting
     * Allowed fields are marked with {@link Sortable}
     * Nested objects Fields expected to have a prefix of the nested field name:
     * <p>
     * <code>
     * class A {
     * \@Sortable(nested = true, targetTableAlias = b_table)
     * B b;
     * }
     * class B {
     * \@Sortable(column = "column_c")
     * String c;
     * }
     * </code>
     * </p>
     * Sort field dto for field c is b.c
     * Mapped sort field is b_table.column_c
     **/
    public <T> List<String> mapSortingFields(List<String> sortFields, Class<T> clazz) {
        List<String> mappedFields = new ArrayList<>();
        List<String> forbiddenFields = new ArrayList<>();

        val sortableFields = getSortableFields(clazz);
        sortFields.forEach(dtoField -> {
            String domainColumn = sortableFields.get(dtoField);
            if (domainColumn != null) {
                mappedFields.add(domainColumn);
            } else {
                forbiddenFields.add(dtoField);
            }
        });

        if (!forbiddenFields.isEmpty()) {
            throw new SortValidationException(forbiddenFields);
        }
        return mappedFields;
    }

    public <T> Map<String, String> getSortableFields(Class<T> clazz) {
        return getSortableFields("", "", clazz);
    }

    private <T> Map<String, String> getSortableFields(String dtoPrefix, String domainPrefix, Class<T> clazz) {
        Map<String, String> sortableFields = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Sortable.class)) {
                val annotation = field.getAnnotation(Sortable.class);
                if (annotation.nested()) {
                    Class<?> nestedType = Collection.class.isAssignableFrom(field.getType()) ?
                            annotation.nestedCollectionItemType() : field.getType();
                    String dtoField = addPrefix(dtoPrefix, field.getName());
                    val nestedSortableFields = getSortableFields(dtoField, annotation.targetTableAlias(), nestedType);
                    sortableFields.putAll(nestedSortableFields);
                    continue;
                }

                String dtoField = addPrefix(dtoPrefix, field.getName());
                String domainColumn = StringUtils.isNotBlank(annotation.column()) ? annotation.column() : field.getName();
                String domainField = addPrefix(domainPrefix, domainColumn);

                sortableFields.put(dtoField, domainField);
            }
        }
        return sortableFields;
    }

    private static String addPrefix(String prefix, String str) {
        return StringUtils.isNotBlank(prefix) ? prefix + "." + str : str;
    }
}
