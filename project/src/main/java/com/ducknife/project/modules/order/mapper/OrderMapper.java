package com.ducknife.project.modules.order.mapper;

import org.mapstruct.Mapper;

import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.order.dto.OrderResponse;
import com.ducknife.project.modules.orderdetail.mapper.OrderDetailMapper;
import com.ducknife.project.modules.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {OrderDetailMapper.class, UserMapper.class})
public interface OrderMapper {
    OrderResponse toResponse(Order order);
    // Order toEntity(OrderRequest request);
} 
