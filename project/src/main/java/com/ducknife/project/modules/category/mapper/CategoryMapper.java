package com.ducknife.project.modules.category.mapper;

import org.mapstruct.Mapper;

import com.ducknife.project.modules.category.Category;
import com.ducknife.project.modules.category.dto.CategoryDTO;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDto(Category category);
}

/* MapStruct dùng khi chuyển từ Entity -> Response DTO vì khi đó entity đã có đủ dữ liệu 
từ Request DTO -> Entity thì chỉ khì field đó khớp tên/kiểu, nếu cần tra db thì ignore = true rồi set riêng
Khi entity/DTO có quan hệ lồng nhau cần convert thì dùng uses = {các class mapper}
*/