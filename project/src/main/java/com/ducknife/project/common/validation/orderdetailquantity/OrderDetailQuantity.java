package com.ducknife.project.common.validation.orderdetailquantity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@NotNull(message = "{orderdetail.quantity.notnull}")
@Positive(message = "{orderdetail.quantity.positive}")
// NotNull = khác null
// NotEmpty = NotNull + size() > 0
// NotBlank = NotNull + trim().size() > 0
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface OrderDetailQuantity {
    String message() default "{orderdetail.quantity.notnull}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
