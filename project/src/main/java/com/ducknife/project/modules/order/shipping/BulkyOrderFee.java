package com.ducknife.project.modules.order.shipping;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class BulkyOrderFee implements ShippingStrategy {
    
    @Override
    public boolean supports(BigDecimal subtotal, int totalQuantity) {
        return totalQuantity >= 10;
    }

    @Override
    public BigDecimal fee() {
        return new BigDecimal("50000");
    }

    @Override
    public int priority() {
        return 2;
    }
}
