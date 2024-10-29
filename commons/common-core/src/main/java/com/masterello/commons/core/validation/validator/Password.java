package com.masterello.commons.core.validation.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message() default "Invalid password";

    int min() default 8;
    int max() default 20;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
