package com.ducknife.project.modules.orderdetail.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.orderdetail.OrderDetail;
import com.ducknife.project.modules.orderdetail.dto.OrderDetailRequest;
import com.ducknife.project.modules.orderdetail.dto.OrderDetailResponse;
import com.ducknife.project.modules.product.Product;
import com.ducknife.project.modules.product.mapper.ProductMapper;

@Mapper(componentModel = "spring", uses = { ProductMapper.class })
public interface OrderDetailMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "price", source = "product.price")
    OrderDetail toEntity(OrderDetailRequest orderRequest, Product product, Order order);

    OrderDetailResponse toResponse(OrderDetail orderDetail);
}
