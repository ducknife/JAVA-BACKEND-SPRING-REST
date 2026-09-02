package com.ducknife.project.modules.order;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ResponseFactory;
import com.ducknife.project.modules.order.action.OrderActionType;
import com.ducknife.project.modules.order.dto.OrderRequest;
import com.ducknife.project.modules.order.dto.OrderResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ResponseFactory<List<OrderResponse>>> getOrders() {
        return ResponseFactory.ok(orderService.getOrders());
    }

    @GetMapping("/count")
    public ResponseEntity<ResponseFactory<Long>> countOrders() {
        return ResponseFactory.ok(orderService.countOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseFactory<OrderResponse>> getOrderById(@PathVariable Long id) {
        return ResponseFactory.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<ResponseFactory<OrderResponse>> addOrder(@RequestBody @Valid OrderRequest order) {
        OrderResponse savedOrder = orderService.add(order);
        return ResponseFactory.created(savedOrder);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.performAction(id, OrderActionType.CANCEL);
        return ResponseEntity.noContent().build();
    }

}
