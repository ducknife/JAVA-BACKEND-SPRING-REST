package com.ducknife.project.modules.order.discount;

import java.math.BigDecimal;

import com.ducknife.project.modules.user.User;

public interface DiscountStrategy {
    boolean supports(User user, BigDecimal subtotal);
    BigDecimal discountRate();
}
