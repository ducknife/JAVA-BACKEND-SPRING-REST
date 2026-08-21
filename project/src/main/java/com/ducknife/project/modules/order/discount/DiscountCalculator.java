package com.ducknife.project.modules.order.discount;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ducknife.project.modules.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountCalculator {

    private final List<DiscountStrategy> strategies;

    public BigDecimal rateFor(User user, BigDecimal subtotal) {
        BigDecimal discountRate = BigDecimal.ZERO;
        for (DiscountStrategy ds : strategies) {
            if (ds.supports(user, subtotal)) {
                discountRate = discountRate.add(ds.discountRate());
            }
        }
        if (discountRate.compareTo(new BigDecimal("0.3")) > 0) {
            discountRate = new BigDecimal("0.3");
        }
        return discountRate;
    }
}
