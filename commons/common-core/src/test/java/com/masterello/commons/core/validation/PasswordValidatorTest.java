package com.masterello.commons.core.validation;

import com.masterello.commons.core.validation.validator.Password;
import com.masterello.commons.core.validation.validator.PasswordValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PasswordValidatorTest {

    private PasswordValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;


    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        // Configure the mock context to return a mock ConstraintViolationBuilder
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);


        Password annotation = mock(Password.class);
        when(annotation.min()).thenReturn(10);
        when(annotation.max()).thenReturn(20);
        validator.initialize(annotation);
    }

    @Test
    void shouldFailForNullPassword() {
        assertFalse(validator.isValid(null, context), "Password should be invalid when null");
    }

    @Test
    void shouldFailForPasswordTooShort() {
        assertFalse(validator.isValid("Short1!", context), "Password should be invalid when too short");
    }

    @Test
    void shouldFailForPasswordTooLong() {
        assertFalse(validator.isValid("ThisIsAVeryLongPassword1!", context), "Password should be invalid when too long");
    }

    @Test
    void shouldFailForMissingUppercase() {
        assertFalse(validator.isValid("lowercase1!", context), "Password should be invalid without uppercase letter");
    }

    @Test
    void shouldFailForMissingLowercase() {
        assertFalse(validator.isValid("UPPERCASE1!", context), "Password should be invalid without lowercase letter");
    }

    @Test
    void shouldFailForMissingDigit() {
        assertFalse(validator.isValid("NoDigitsHere!", context), "Password should be invalid without a digit");
    }

    @Test
    void shouldFailForMissingSpecialCharacter() {
        assertFalse(validator.isValid("NoSpecial1", context), "Password should be invalid without special character");
    }

    @Test
    void shouldPassForValidPassword() {
        assertTrue(validator.isValid("ValidPass1!", context), "Password should be valid when all criteria are met");
    }

    @Test
    void shouldPassForValidPasswordWithDot() {
        assertTrue(validator.isValid("ValidPass1.", context), "Password should be valid when all criteria are met");
    }
}