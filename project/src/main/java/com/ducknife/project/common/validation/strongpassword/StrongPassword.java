package com.ducknife.project.common.validation.strongpassword;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

// 3 annotation này bắt buộc (FIELD áp dụng cho 1 trường, PARAMETER là cho 1 tham số/biến, ngoài ra còn các kiểu khác)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {

    // 3 method này là bắt buộc phải có của 1 custom validation
    String message() default "Mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt, tối thiểu 8 ký tự";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
