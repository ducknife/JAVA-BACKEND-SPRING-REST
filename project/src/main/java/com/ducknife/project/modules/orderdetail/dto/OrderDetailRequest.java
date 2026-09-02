package com.ducknife.project.modules.orderdetail.dto;

import com.ducknife.project.common.validation.orderdetailquantity.OrderDetailQuantity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class OrderDetailRequest {
    @NotNull(message = "{orderdetail.product.notnull}")
    private Long productId;
    @OrderDetailQuantity
    private Long quantity;
}
