package com.masterello.validation;


import com.masterello.auth.data.AuthZRole;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AuthZRule {

    AuthZRole[] roles();

    boolean isOwner() default false;

    boolean isEmailVerified() default false;
}
