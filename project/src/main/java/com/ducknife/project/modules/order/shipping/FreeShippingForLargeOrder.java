package com.ducknife.project.modules.order.shipping;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class FreeShippingForLargeOrder implements ShippingStrategy {
    
    @Override
    public boolean supports(BigDecimal subtotal, int totalQuantity) {
        return subtotal.compareTo(new BigDecimal("500000")) >= 0;
    }

    @Override
    public BigDecimal fee() {
        return BigDecimal.ZERO;
    }

    @Override
    public int priority() {
        return 1;
    }
}
