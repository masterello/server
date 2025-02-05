package com.masterello.commons.security.serialization;

import com.masterello.auth.data.AuthZRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation which restricts access to specific fields of the DTOs
 * Field marked with this annotation will only be serialized for authenticated user.
 * Optionally roles may be restricted as well
 */
@Retention(value = RUNTIME)
@Target(ElementType.FIELD)
public @interface AuthGuard {
    AuthZRole[] roles() default {};
}