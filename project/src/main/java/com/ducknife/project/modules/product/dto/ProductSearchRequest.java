package com.ducknife.project.modules.product.dto;

import com.ducknife.project.common.validation.validpricerange.ValidPriceRange;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ValidPriceRange
public class ProductSearchRequest {
    @NotBlank(message = "{product.search.name.notblank}")
    private String name; 
    @DecimalMin(value = "0.0", message = "{product.search.minPrice.min}")
    private double minPrice;
    @DecimalMin(value = "0.0", message = "{product.search.maxPrice.min}")
    private double maxPrice;
}