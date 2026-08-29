package com.ducknife.project.modules.order.shipping;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class DefaultOrderFee implements ShippingStrategy {
    
    @Override
    public boolean supports(BigDecimal subtotal, int totalQuantity) {
        return true;
    }

    @Override
    public BigDecimal fee() {
        return new BigDecimal("30000");
    }

    @Override
    public int priority() {
        return 3;
    }
}
