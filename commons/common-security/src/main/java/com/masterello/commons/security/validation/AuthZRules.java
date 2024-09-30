package com.masterello.commons.security.validation;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AuthZRules {
    AuthZRule[] value();
}
