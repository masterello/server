package com.masterello.worker.validator;

import com.masterello.category.dto.CategoryDto;
import com.masterello.category.service.ReadOnlyCategoryService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.masterello.commons.core.validation.ErrorCodes.SERVICE_ID_EMPTY;
import static com.masterello.commons.core.validation.ErrorCodes.SERVICE_ID_NOT_FOUND;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceValidator implements ConstraintValidator<ServiceId, Integer> {

    private final ReadOnlyCategoryService categoryService;

    @Override
    public boolean isValid(Integer serviceId, ConstraintValidatorContext context) {
        if (serviceId == null) {
            return addViolation(context, SERVICE_ID_EMPTY);
        }
        boolean valid = getActiveCategories().contains(serviceId);
        if (!valid) {
            addViolation(context, SERVICE_ID_NOT_FOUND);
        }
        return valid;
    }

    private Set<Integer> getActiveCategories() {
        return categoryService.getAllCategories().stream()
                .filter(category -> Optional.ofNullable(category.getActive()).orElse(false))
                .map(CategoryDto::getCategoryCode)
                .collect(Collectors.toSet());
    }

    private boolean addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
        return false;
    }
}
