package com.ducknife.project.modules.product.dto;

import java.math.BigDecimal;

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
    @NotNull(message = "{product.price.notnull}")
    @DecimalMin(value = "1000.0", message = "{product.price.min}")
    @Digits(integer = 10, fraction = 2, message = "{product.price.digits}")
    private BigDecimal price;
    @NotNull(message = "{product.category.notnull}")
    private Long category_id;
}
