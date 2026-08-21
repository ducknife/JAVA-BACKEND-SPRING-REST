package com.ducknife.project.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.ducknife.project.modules.order.discount.LargeOrderDiscount;

public class LargeOrderDiscountTest {
    
    private final LargeOrderDiscount strategy = new LargeOrderDiscount();

    @Test
    void orderGreaterThan1M_apply() {
        assertTrue(strategy.supports(null, new BigDecimal("1000001")));
    }

    @Test
    void order1M_apply() {
        assertTrue(strategy.supports(null, new BigDecimal("1000000")));
    }

    @Test
    void orderLessThan1M_notApply() {
        assertFalse(strategy.supports(null, new BigDecimal("999999")));
    }
}
