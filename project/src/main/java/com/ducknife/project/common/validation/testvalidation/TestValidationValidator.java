package com.ducknife.project.common.validation.testvalidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TestValidationValidator implements ConstraintValidator<TestValidation, String> {
    
    @Override
    public boolean isValid(String testText, ConstraintValidatorContext context) {
        return !testText.isBlank() && !testText.isEmpty();
    }
}
