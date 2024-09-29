package com.masterello.commons.core.sort;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sortable {

    boolean nested() default false;

    Class<?> nestedCollectionItemType() default void.class;

    String targetTableAlias() default "";

    String column() default "";
}