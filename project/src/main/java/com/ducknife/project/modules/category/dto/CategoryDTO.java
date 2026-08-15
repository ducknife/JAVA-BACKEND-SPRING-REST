package com.ducknife.project.modules.category.dto;

import com.ducknife.project.modules.category.Category;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    @NotBlank(message = "{category.name.notblank}")
    private String name;
    private Long id;

    public static CategoryDTO from(Category category) {
        return CategoryDTO.builder()    
                        .name(category.getName())
                        .id(category.getId())
                        .build();
    }
}
