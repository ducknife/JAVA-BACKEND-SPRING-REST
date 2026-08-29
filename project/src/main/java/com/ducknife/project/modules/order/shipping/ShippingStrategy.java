package com.ducknife.project.modules.order.shipping;

import java.math.BigDecimal;

public interface ShippingStrategy {
    boolean supports(BigDecimal subtotal, int totalQuantity);
    BigDecimal fee();
    int priority();
}
