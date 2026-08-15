package com.ducknife.project.common.validation.passwordmatch;

import com.ducknife.project.modules.auth.dto.ChangePasswordRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, ChangePasswordRequest> {
    @Override
    public boolean isValid(ChangePasswordRequest request, ConstraintValidatorContext context) {
        if (request.getNewPassword() == null)
            return true;

        boolean matched = request.getNewPassword().equals(request.getConfirmPassword());

        if (!matched) {
            // bỏ violation mặc định 
            context.disableDefaultConstraintViolation();
            
            // build violation mới
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword") // xác định giao diện hiển thị lỗi ở field nào 
                    .addConstraintViolation();
        }
        return matched;
    }
}
