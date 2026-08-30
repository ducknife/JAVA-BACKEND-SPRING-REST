package com.ducknife.project.common.validation.testvalidation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TestValidationValidator.class)
public @interface TestValidation {

    String message() default "{test.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
