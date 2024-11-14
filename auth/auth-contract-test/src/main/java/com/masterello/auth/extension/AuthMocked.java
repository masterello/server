package com.masterello.auth.extension;

import com.masterello.auth.data.AuthZRole;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AuthMocked {
    String userId();
    AuthZRole[] roles() default {AuthZRole.USER};

    boolean emailVerified() default true;

}

