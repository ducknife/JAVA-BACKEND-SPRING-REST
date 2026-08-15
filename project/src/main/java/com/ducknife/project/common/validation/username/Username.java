package com.ducknife.project.common.validation.username;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@NotBlank(message = "{user.username.notblank}")
@Size(min = 3, max = 50, message = "{user.username.size}")
@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "{user.username.pattern}")
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface Username {
    String message() default "{user.username.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
