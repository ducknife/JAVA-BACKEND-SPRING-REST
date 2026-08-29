package com.ducknife.project.modules.order.action;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ducknife.project.common.exception.InvalidRequestException;

// factory là nơi chọn ra handler tương ứng mà che đi các chỗ khởi tạo đối tượng 
@Component
public class OrderActionFactory {

    private final Map<OrderActionType, OrderActionHandler> handlers;

    public OrderActionFactory(List<OrderActionHandler> list) {
        this.handlers = list.stream()
                .collect(Collectors.toMap(OrderActionHandler::getType, Function.identity()));
    }

    public OrderActionHandler get(OrderActionType type) {
        OrderActionHandler handler = handlers.get(type);
        if (handler == null) {
            throw new InvalidRequestException("Không hỗ trợ hành động này: " + type);
        }
        return handler;
    }
}
