package com.ducknife.project.common.validation.validpricerange;

import com.ducknife.project.modules.product.dto.ProductSearchRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPriceRangeValidator implements ConstraintValidator<ValidPriceRange, ProductSearchRequest> {

    @Override
    public boolean isValid(ProductSearchRequest request, ConstraintValidatorContext context) {
        boolean valid = request.getMinPrice() <= request.getMaxPrice();
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("maxPrice") // sẽ báo lỗi ở field maxPrice
                    .addConstraintViolation();
        }
        return valid;
    }
}
