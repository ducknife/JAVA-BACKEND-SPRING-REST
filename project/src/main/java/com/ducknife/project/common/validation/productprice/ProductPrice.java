package com.ducknife.project.common.validation.productprice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

@NotNull(message = "{product.price.notnull}")
@DecimalMin(value = "1000.0", message = "{product.price.min}")
@Digits(integer = 10, fraction = 2, message = "{product.price.digits}")
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ProductPrice {
    String message() default "{product.price.notnull}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
