package com.ducknife.project.modules.order.shipping;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShippingFeeCalculator {

    private final List<ShippingStrategy> strategies;

    public BigDecimal feeFor(BigDecimal subtotal, int totalQuantity) {
        return strategies.stream()
                .filter(s -> s.supports(subtotal, totalQuantity))
                .min(Comparator.comparingInt(ShippingStrategy::priority))
                .map(ShippingStrategy::fee)
                .orElse(BigDecimal.ZERO);
    }
}
