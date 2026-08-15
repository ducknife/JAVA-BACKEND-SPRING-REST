package com.ducknife.project.modules.order.dto;

import java.util.List;

import com.ducknife.project.modules.orderdetail.dto.OrderDetailRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class OrderRequest {
    @NotNull(message = "{order.userid.notnull}")
    private Long userId;
    @Valid
    @NotEmpty(message = "{order.items.notempty}")
    private List<OrderDetailRequest> orderDetails;
}
