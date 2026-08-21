package com.ducknife.project.modules.order.discount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ducknife.project.modules.user.User;

@Component
public class NoDiscount implements DiscountStrategy {
    
    @Override
    public boolean supports(User user, BigDecimal subtotal) {
        return true;
    }

    @Override
    public BigDecimal discountRate() {
        return BigDecimal.ZERO;
    }

}
