package com.ducknife.project.modules.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ducknife.project.modules.category.mapper.CategoryMapper;
import com.ducknife.project.modules.product.Product;
import com.ducknife.project.modules.product.dto.ProductRequest;
import com.ducknife.project.modules.product.dto.ProductResponse;

// Cái Mapper nào có thể map field thành field mong muốn thì cho nó vào uses = {} 
// Nên dùng Mapper tự động này khi đã có đủ dữ liệu, các trường/phần cần query DB thì nên để ignore = true
// Vì cái đó sẽ lo sau.
@Mapper(componentModel = "spring", uses = { CategoryMapper.class })
public interface ProductMapper {
    ProductResponse toResponse(Product product);
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);
}
