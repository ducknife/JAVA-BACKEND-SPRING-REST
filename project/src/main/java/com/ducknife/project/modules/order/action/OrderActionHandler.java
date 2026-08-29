package com.ducknife.project.modules.order.action;

import com.ducknife.project.modules.order.Order;

public interface OrderActionHandler {
    OrderActionType getType();
    void handle(Order order);
}
