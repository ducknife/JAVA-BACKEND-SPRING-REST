package com.ducknife.project.modules.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ducknife.project.modules.category.mapper.CategoryMapper;
import com.ducknife.project.modules.product.Product;
import com.ducknife.project.modules.product.dto.ProductRequest;
import com.ducknife.project.modules.product.dto.ProductResponse;

@Mapper(componentModel = "spring", uses = { CategoryMapper.class })
public interface ProductMapper {
    ProductResponse toResponse(Product product);
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);
}
