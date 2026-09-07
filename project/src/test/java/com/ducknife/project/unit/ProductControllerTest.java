package com.ducknife.project.unit;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.Media;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ducknife.project.modules.category.dto.CategoryDTO;
import com.ducknife.project.modules.product.Product;
import com.ducknife.project.modules.product.ProductController;
import com.ducknife.project.modules.product.ProductService;
import com.ducknife.project.modules.product.dto.ProductRequest;
import com.ducknife.project.modules.product.dto.ProductResponse;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    @WithMockUser
    public void layDanhSachProduct() throws Exception {

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Product Response")
                .build();

        List<ProductResponse> responses = List.of(response);

        when(productService.getProducts()).thenReturn(responses);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Product Response"));
    }

    @Test
    @WithMockUser
    public void themDanhSachProduct() throws Exception {

        ProductRequest request = ProductRequest.builder()
                .name("Product Test")
                .category_id(1L)
                .price(new BigDecimal("100000.00"))
                .build();

        CategoryDTO category = CategoryDTO.builder()
                .id(1L)
                .name("Category Test")
                .build();

        ProductResponse reponse = ProductResponse.builder()
                .id(1L)
                .name("Product Test")
                .price(new BigDecimal("100000.00"))
                .category(category)
                .build();

        when(productService.addProduct(request)).thenReturn(reponse);

        // mockMvc.perform(post("/api/products")
        // .with(csrf())
        // .contentType(MediaType.APPLICATION_JSON)
        // .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Product Test"))
                .andExpect(jsonPath("$.data.price").value(100000.00));
    }
}
