package com.ducknife.project.modules.order.discount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ducknife.project.modules.order.OrderRepository;
import com.ducknife.project.modules.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FirstOrderOfUserDiscount implements DiscountStrategy {
    
    private final OrderRepository orderRepository;

    @Override
    public boolean supports(User user, BigDecimal subtotal) {
        return !orderRepository.existsByUserId(user.getId());
    }

    @Override
    public BigDecimal discountRate() {
        return new BigDecimal("0.08");
    }
}
