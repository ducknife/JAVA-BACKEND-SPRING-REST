package com.ducknife.project.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.config.properties.DataSourceProperties;
import com.ducknife.project.modules.auditlog.AuditService;
import com.ducknife.project.modules.category.CategoryRepository;
import com.ducknife.project.modules.product.Product;
import com.ducknife.project.modules.product.ProductRepository;
import com.ducknife.project.modules.product.ProductService;
import com.ducknife.project.modules.product.dto.ProductResponse;
import com.ducknife.project.modules.product.mapper.ProductMapper;

@ExtendWith(MockitoExtension.class)
public class ProductTest {
    
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private ProductMapper productMapper;
    
    @InjectMocks
    private ProductService productService;

    private Product product;
    private Product otherProduct;
    private ProductResponse response;
    
    @BeforeEach
    public void setUp() {
        product = Product.builder().id(1L).build();
        otherProduct = Product.builder().id(2L).build();
        response = ProductResponse.builder().id(1L).build();
    }

    @Test
    @DisplayName("Lấy danh sách sản phẩm")
    public void layDanhSachSanPham() {

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        List<ProductResponse> results = productService.getProducts();

        assertEquals(1L, results.size());
        assertEquals(1L, results.get(0).getId());

        assertNull(product.getCategory());
        assertNotNull(product);
    }

    @Nested
    @DisplayName("Lấy sản phẩm theo Id")
    class laySanPhamTheoIdTest {
        @Test
        @DisplayName("Sản phẩm tồn tại")
        public void timThaySanPham() {

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productMapper.toResponse(product)).thenReturn(response);

            ProductResponse result = productService.getProductById(1L);

            assertEquals(1L, result.getId());

            verify(productRepository, times(1)).findById(1L);
            verify(productMapper, times(1)).toResponse(product);
        }

        @Test
        @DisplayName("Sản phẩm không tồn tại")
        public void khongTimThaySanPham() {

            when(productRepository.findById(999L)).thenReturn(Optional.empty());
            
            assertThrows(ResourceNotFoundException.class, () -> {
                productService.getProductById(999L);
            });

            verify(productRepository, times(1)).findById(999L);
            verify(productMapper, never()).toResponse(any());
        }
    }

    @Test
    @DisplayName("Lấy sản phẩm trong khoảng giá")
    public void laySanPhamTrongKhoangGia() {

        when(productRepository.findByNameAndPrice("Machine Learning", 100000.00, 150000.00))
            .thenReturn(List.of(product, otherProduct));

        List<ProductResponse> results = productService.getProductsByNameAndPrice("Machine Learning", 100000.00, 150000.00);

        assertTrue(results.size() == 2);

        verify(productRepository, times(1)).findByNameAndPrice("Machine Learning", 100000.00, 150000.00);
        verify(productMapper, times(2)).toResponse(any());
    }
}
