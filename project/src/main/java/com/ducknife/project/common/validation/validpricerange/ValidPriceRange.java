package com.ducknife.project.common.validation.validpricerange;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPriceRangeValidator.class)
public @interface ValidPriceRange {
    String message() default "{validation.pricerange}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
