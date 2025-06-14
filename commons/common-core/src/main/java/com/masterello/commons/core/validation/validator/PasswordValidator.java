package com.masterello.commons.core.validation.validator;

import com.masterello.commons.core.validation.ErrorCodes;
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
                        context, ErrorCodes.PASSWORD_LENGTH),
                () -> validateCondition(password.matches(".*[A-Z].*"),
                        context, ErrorCodes.PASSWORD_UPPERCASE),
                () -> validateCondition(password.matches(".*[a-z].*"),
                        context, ErrorCodes.PASSWORD_LOWERCASE),
                () -> validateCondition(password.matches(".*\\d.*"),
                        context, ErrorCodes.PASSWORD_DIGIT),
                () -> validateCondition(password.matches(".*[!@#$%^&*().].*"),
                        context, ErrorCodes.PASSWORD_SPECIAL_CHAR)
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
