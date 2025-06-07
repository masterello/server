package com.masterello.worker.validator;

import com.masterello.category.dto.CategoryDto;
import com.masterello.category.service.ReadOnlyCategoryService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.masterello.commons.core.validation.ErrorCodes.SERVICE_ID_EMPTY;
import static com.masterello.commons.core.validation.ErrorCodes.SERVICE_ID_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class ServiceValidator implements ConstraintValidator<ServiceId, Integer> {

    private final ReadOnlyCategoryService categoryService;
    private volatile Set<Integer> activeCategories;

    @PostConstruct
    public void init() {
        activeCategories = categoryService.getAllCategories().stream()
                .filter(category -> Optional.ofNullable(category.getActive()).orElse(false))
                .map(CategoryDto::getCategoryCode)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Integer serviceId, ConstraintValidatorContext context) {
        if (serviceId == null) {
            return addViolation(context, SERVICE_ID_EMPTY);
        }

        boolean valid = activeCategories.contains(serviceId);
        if (!valid) {
            addViolation(context, SERVICE_ID_NOT_FOUND);
        }
        return valid;
    }

    private boolean addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
        return false;
    }
}
