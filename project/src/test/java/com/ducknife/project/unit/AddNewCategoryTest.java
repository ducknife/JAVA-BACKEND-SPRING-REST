package com.ducknife.project.unit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ducknife.project.modules.category.CategoryController;
import com.ducknife.project.modules.category.CategoryService;
import com.ducknife.project.modules.category.dto.CategoryDTO;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CategoryController.class) // test tầng controller, thêm class để chỉ load controller đó
public class AddNewCategoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean // mock bean cần thiết (hoạt động giống @Mock)
    private CategoryService categoryService;

    @Test
    @WithMockUser // giả lập đăng nhập rồi
    public void themDanhMucMoi() throws Exception {
        CategoryDTO newCategory = CategoryDTO.builder().build();

        mockMvc.perform(post("/api/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void layThongTinDanhMuc() throws Exception {
        CategoryDTO category = CategoryDTO.builder()
                .id(1L)
                .name("MLB")
                .build();
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("MLB"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }
}
