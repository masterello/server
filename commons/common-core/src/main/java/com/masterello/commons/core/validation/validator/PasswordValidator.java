package com.masterello.commons.core.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.function.BooleanSupplier;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private int min;
    private int max;

    @Override
    public void initialize(Password constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return addViolation(context, "Password cannot be null");
        }

        List<BooleanSupplier> conditions = List.of(
                () -> validateCondition(password.length() >= min && password.length() <= max,
                        context, String.format("Password must be between %d and %d characters", min, max)),
                () -> validateCondition(password.matches(".*[A-Z].*"),
                        context, "Password must contain at least one uppercase letter"),
                () -> validateCondition(password.matches(".*[a-z].*"),
                        context, "Password must contain at least one lowercase letter"),
                () -> validateCondition(password.matches(".*\\d.*"),
                        context, "Password must contain at least one digit"),
                () -> validateCondition(password.matches(".*[!@#$%^&*()].*"),
                        context, "Password must contain at least one special character")
        );

        // Evaluate all conditions and return true only if all are true
        return conditions.stream()
                .map(BooleanSupplier::getAsBoolean)
                .reduce(true, (a, b) -> a & b);
    }

    private boolean validateCondition(boolean condition, ConstraintValidatorContext context, String message) {
        if (!condition) {
            addViolation(context, message);
            return false;
        }
        return true;
    }

    private boolean addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
        return false;
    }
}
