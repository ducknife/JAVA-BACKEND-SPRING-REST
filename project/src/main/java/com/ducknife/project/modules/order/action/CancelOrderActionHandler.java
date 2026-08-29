package com.ducknife.project.modules.order.action;

import org.springframework.stereotype.Component;

import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.order.OrderRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CancelOrderActionHandler implements OrderActionHandler {
    
    private final OrderRepository orderRepository;

    @Override
    public OrderActionType getType() {
        return OrderActionType.CANCEL;
    }

    @Override
    public void handle(Order order) {
        orderRepository.delete(order);
    }
}
