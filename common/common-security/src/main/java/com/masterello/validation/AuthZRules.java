package com.masterello.validation;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AuthZRules {
    AuthZRule[] value();
}
