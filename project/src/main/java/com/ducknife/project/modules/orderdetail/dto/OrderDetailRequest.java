package com.ducknife.project.modules.orderdetail.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @NotNull(message = "{orderdetail.quantity.notnull}")
    @Positive(message = "{orderdetail.quantity.positive}")
    private Long quantity;
}
