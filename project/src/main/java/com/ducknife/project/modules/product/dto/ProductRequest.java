package com.ducknife.project.modules.product.dto;

import java.math.BigDecimal;

import com.ducknife.project.common.validation.productprice.ProductPrice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // cái này = getter + setter + toString + equalsAndHashCode + RequiredArgsConstructor 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    @NotBlank(message = "{product.name.notblank}")
    private String name;
    @ProductPrice
    private BigDecimal price;
    @NotNull(message = "{product.category.notnull}")
    private Long category_id;
}
