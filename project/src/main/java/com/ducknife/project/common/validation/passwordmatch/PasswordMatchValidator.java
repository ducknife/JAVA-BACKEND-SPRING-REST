package com.ducknife.project.common.validation.passwordmatch;

import com.ducknife.project.modules.auth.dto.ChangePasswordRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, ChangePasswordRequest> {
    @Override
    public boolean isValid(ChangePasswordRequest request, ConstraintValidatorContext context) {
        if (request.getNewPassword() == null) return true;
        return request.getNewPassword().equals(request.getConfirmPassword());
    }
}
