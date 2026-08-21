package com.ducknife.project.modules.order.discount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ducknife.project.modules.user.User;

@Component
public class LargeOrderDiscount implements DiscountStrategy {

    @Override
    public boolean supports(User user, BigDecimal subtotal) {
        return subtotal.compareTo(new BigDecimal("1000000")) >= 0;
    }

    @Override
    public BigDecimal discountRate() {
        return new BigDecimal("0.05");
    }
}
