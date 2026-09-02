# 🧪 Phase 5.3 — Testing trong Spring Boot

> **Bài 15** · JUnit 5 · Mockito · @WebMvcTest · @DataJpaTest · Testcontainers

---

## 📑 Mục Lục

1. [Tại sao phải Test?](#1-tại-sao-phải-test)
2. [Kim tự tháp Testing](#2-kim-tự-tháp-testing)
3. [JUnit 5 — Nền tảng](#3-junit-5--nền-tảng)
4. [Mockito — Mock dependency](#4-mockito--mock-dependency)
5. [Unit Test Service Layer (Mockito)](#5-unit-test-service-layer-mockito)
6. [@WebMvcTest — Test Controller Layer](#6-webmvctest--test-controller-layer)
7. [@DataJpaTest — Test Repository Layer](#7-datajpatest--test-repository-layer)
8. [Testcontainers — Test với DB thật](#8-testcontainers--test-với-db-thật)
9. [Tổng kết & Best Practice](#9-tổng-kết--best-practice)

---

## 1. Tại sao phải Test?

```
Code không có test = Code đang chờ nổ
```

| Lý do | Giải thích |
|-------|-----------|
| **Phát hiện bug sớm** | Chạy test → thấy lỗi ngay, không cần deploy lên server rồi mới biết |
| **Refactor an toàn** | Thay đổi logic → chạy test → nếu pass = không phá gì cũ |
| **Documentation sống** | Đọc test biết code hoạt động thế nào, rõ hơn comment |
| **CI/CD bắt buộc** | Pipeline sẽ chạy test trước khi deploy, fail test = không deploy |
| **CV/phỏng vấn** | Viết test = điểm cộng rất lớn khi xin việc |

---

## 2. Kim tự tháp Testing

```
            ┌──────────────────┐
            │   E2E / Manual   │  ← Ít nhất, chậm nhất, đắt nhất
            ├──────────────────┤
            │  Integration Test│  ← @WebMvcTest, @DataJpaTest, Testcontainers
            ├──────────────────┤
            │    Unit Test     │  ← Nhiều nhất, nhanh nhất, rẻ nhất
            └──────────────────┘
```

| Loại | Đặc điểm | Ví dụ trong project của bạn |
|------|----------|---------------------------|
| **Unit Test** | Test 1 class, mock hết dependency, **không cần Spring** | Test `ProductService` → mock `ProductRepository` |
| **Integration Test** | Test nhiều class phối hợp, **có Spring** | `@WebMvcTest` test Controller + Security, `@DataJpaTest` test Repository + DB |
| **E2E Test** | Test toàn bộ flow, **cần server chạy thật** | Postman, Selenium (chưa cần ở giai đoạn này) |

> 💡 **Nguyên tắc**: Viết **nhiều Unit Test**, **vừa đủ Integration Test**, **ít E2E Test**

---

## 3. JUnit 5 — Nền tảng

### 3.1. JUnit 5 là gì?

JUnit 5 = **JUnit Platform** + **JUnit Jupiter** + **JUnit Vintage**

Bạn chỉ cần quan tâm **JUnit Jupiter** — đây là API mà bạn dùng để viết test.

Spring Boot đã include sẵn JUnit 5 qua dependency `spring-boot-starter-test` (trong `pom.xml` của bạn là `spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`... đều kéo theo JUnit 5).

### 3.2. Cấu trúc 1 file test

```java
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class MyTest {

    @BeforeAll   // chạy 1 lần trước TẤT CẢ test — phải static
    static void setupAll() { }

    @BeforeEach  // chạy trước MỖI test method
    void setup() { }

    @Test        // đánh dấu đây là 1 test case
    @DisplayName("Mô tả test bằng tiếng Việt cũng được")
    void testSomething() {
        // Arrange — chuẩn bị dữ liệu
        // Act    — gọi method cần test
        // Assert — kiểm tra kết quả
    }

    @AfterEach   // chạy sau MỖI test method
    void tearDown() { }

    @AfterAll    // chạy 1 lần sau TẤT CẢ test — phải static
    static void tearDownAll() { }
}
```

> 🧠 **Nhớ**: Thứ tự chạy là `@BeforeAll` → (`@BeforeEach` → `@Test` → `@AfterEach`) × N → `@AfterAll`

### 3.3. Các Assertion quan trọng

```java
// So sánh giá trị
assertEquals(expected, actual);
assertEquals(expected, actual, "Message khi fail");

// Kiểm tra null
assertNull(object);
assertNotNull(object);

// Kiểm tra boolean
assertTrue(condition);
assertFalse(condition);

// Kiểm tra exception — RẤT HAY DÙNG
assertThrows(ResourceNotFoundException.class, () -> {
    productService.getProductById(999L);
});

// Kiểm tra exception + lấy message
ResourceNotFoundException ex = assertThrows(
    ResourceNotFoundException.class,
    () -> productService.getProductById(999L)
);
assertEquals("Không tìm thấy sản phẩm!", ex.getMessage());

// So sánh object (dùng equals)
assertNotEquals(obj1, obj2);

// Kiểm tra list
assertIterableEquals(expectedList, actualList);
```

### 3.4. Annotation bổ sung

```java
@Disabled("Lý do tạm skip")       // Bỏ qua test này
@RepeatedTest(5)                   // Chạy lại 5 lần
@Timeout(value = 2, unit = SECONDS) // Fail nếu chạy quá 2s

// Test theo điều kiện
@EnabledOnOs(OS.WINDOWS)
@EnabledForJreRange(min = JRE.JAVA_21)

// Nhóm test
@Tag("slow")
@Tag("integration")
```

### 3.5. @ParameterizedTest — Test nhiều bộ dữ liệu

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ParameterizedTest
@DisplayName("Kiểm tra phí ship theo subtotal và quantity")
@CsvSource({
    "100000, 2,  30000",   // đơn nhỏ → phí mặc định
    "600000, 2,  0",       // đơn ≥ 500k → miễn phí
    "100000, 12, 50000",   // ≥ 10 SP → phí cồng kềnh
    "600000, 12, 0"        // vừa lớn vừa nhiều → miễn phí
})
void testShippingFee(String subtotal, int qty, String expectedFee) {
    BigDecimal actual = calculator.feeFor(new BigDecimal(subtotal), qty);
    assertEquals(0, new BigDecimal(expectedFee).compareTo(actual));
}
```

> Bạn đã dùng cách viết test riêng từng case trong `ShippingFeeCalculatorTest` — `@ParameterizedTest` giúp gộp lại gọn hơn khi logic giống nhau.

---

## 4. Mockito — Mock dependency

### 4.1. Tại sao cần Mock?

```
ProductService phụ thuộc vào:
  → ProductRepository (cần DB)
  → CategoryRepository (cần DB)
  → AuditService (cần DB)
  → ProductMapper (cần Spring)
```

Khi **Unit Test** `ProductService`, ta **KHÔNG MUỐN** kết nối DB hay khởi động Spring.
→ Ta **mock** (giả lập) tất cả dependency, chỉ test logic của `ProductService`.

### 4.2. Setup Mockito

```java
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)  // Kích hoạt Mockito
class ProductServiceTest {

    @Mock  // Tạo object giả — không có logic thật
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks  // Tạo ProductService thật, inject các @Mock vào
    private ProductService productService;
}
```

**Giải thích flow:**

```
@Mock ProductRepository       ─┐
@Mock CategoryRepository      ─┤
@Mock AuditService             ─┼─→ @InjectMocks ProductService
@Mock ProductMapper            ─┤
@Mock DataSourceProperties     ─┤     (inject qua constructor)
@Mock ServerProperties         ─┘
```

### 4.3. Các lệnh Mockito quan trọng

```java
// ① WHEN-THEN: Giả lập hành vi
when(productRepository.findById(1L))
    .thenReturn(Optional.of(product));      // trả về giá trị

when(productRepository.findById(999L))
    .thenReturn(Optional.empty());          // trả về empty

when(productRepository.save(any(Product.class)))
    .thenReturn(savedProduct);              // any() = bất kỳ tham số nào

when(productRepository.existsById(1L))
    .thenReturn(true);

// ② VERIFY: Kiểm tra method có được gọi không
verify(productRepository).findById(1L);           // được gọi đúng 1 lần
verify(productRepository, times(1)).save(any());   // gọi đúng 1 lần
verify(productRepository, never()).deleteById(any()); // KHÔNG bao giờ được gọi

// ③ DO-THROW: Giả lập ném exception
when(productRepository.findById(999L))
    .thenThrow(new ResourceNotFoundException("Không tìm thấy!"));

// ④ ARGUMENT CAPTOR: Bắt tham số truyền vào
ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
verify(productRepository).save(captor.capture());
Product savedArg = captor.getValue();
assertEquals("iPhone 16", savedArg.getName());
```

### 4.4. @Mock vs @Spy vs @InjectMocks

| Annotation | Mô tả | Khi nào dùng |
|-----------|--------|-------------|
| `@Mock` | Object hoàn toàn giả, mọi method trả về null/0/false | Mock dependency (repo, mapper...) |
| `@Spy` | Object thật, nhưng có thể override 1 số method | Khi muốn giữ logic thật, chỉ mock 1 phần |
| `@InjectMocks` | Tạo object thật, inject các @Mock/@Spy vào constructor | Class đang test (Service) |

---

## 5. Unit Test Service Layer (Mockito)

### Áp dụng vào `ProductService` của bạn

```java
package com.ducknife.project.modules.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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
import com.ducknife.project.config.properties.ServerProperties;
import com.ducknife.project.modules.auditlog.AuditService;
import com.ducknife.project.modules.category.Category;
import com.ducknife.project.modules.category.CategoryRepository;
import com.ducknife.project.modules.product.dto.ProductRequest;
import com.ducknife.project.modules.product.dto.ProductResponse;
import com.ducknife.project.modules.product.mapper.ProductMapper;

@ExtendWith(MockitoExtension.class)  // ← Không cần Spring, chạy rất nhanh!
class ProductServiceTest {

    // ============ MOCK TẤT CẢ DEPENDENCY ============
    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AuditService auditService;
    @Mock private ProductMapper productMapper;
    @Mock private DataSourceProperties dataSourceProperties;
    @Mock private ServerProperties serverProperties;

    // ============ CLASS ĐANG TEST ============
    @InjectMocks private ProductService productService;

    // ============ DỮ LIỆU DÙNG CHUNG ============
    private Product product;
    private ProductResponse productResponse;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Điện thoại")
                .build();

        product = Product.builder()
                .id(1L)
                .name("iPhone 16")
                .price(new BigDecimal("25000000.00"))
                .category(category)
                .build();

        productResponse = ProductResponse.builder()
                .id(1L)
                .name("iPhone 16")
                .price(new BigDecimal("25000000.00"))
                .build();
    }

    // ================================================
    //  Dùng @Nested để nhóm test theo method
    // ================================================

    @Nested
    @DisplayName("getProductById()")
    class GetProductById {

        @Test
        @DisplayName("Tìm thấy sản phẩm → trả về ProductResponse")
        void whenProductExists_thenReturnResponse() {
            // Arrange — giả lập repo trả về product
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(product));
            when(productMapper.toResponse(product))
                    .thenReturn(productResponse);

            // Act — gọi method thật
            ProductResponse result = productService.getProductById(1L);

            // Assert — kiểm tra kết quả
            assertNotNull(result);
            assertEquals("iPhone 16", result.getName());
            assertEquals(new BigDecimal("25000000.00"), result.getPrice());

            // Verify — kiểm tra repo có được gọi đúng 1 lần
            verify(productRepository, times(1)).findById(1L);
            verify(productMapper, times(1)).toResponse(product);
        }

        @Test
        @DisplayName("Không tìm thấy → ném ResourceNotFoundException")
        void whenProductNotFound_thenThrowException() {
            // Arrange
            when(productRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> productService.getProductById(999L)
            );

            assertEquals("Không tìm thấy sản phẩm!", ex.getMessage());
            verify(productRepository).findById(999L);
            verify(productMapper, never()).toResponse(any()); // mapper không được gọi
        }
    }

    @Nested
    @DisplayName("getProducts()")
    class GetProducts {

        @Test
        @DisplayName("Có sản phẩm → trả về danh sách")
        void whenProductsExist_thenReturnList() {
            // Arrange
            when(productRepository.findAll())
                    .thenReturn(List.of(product));
            when(productMapper.toResponse(product))
                    .thenReturn(productResponse);

            // Act
            List<ProductResponse> results = productService.getProducts();

            // Assert
            assertEquals(1, results.size());
            assertEquals("iPhone 16", results.get(0).getName());
        }

        @Test
        @DisplayName("Không có sản phẩm → trả về list rỗng")
        void whenNoProducts_thenReturnEmptyList() {
            when(productRepository.findAll())
                    .thenReturn(List.of());

            List<ProductResponse> results = productService.getProducts();

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProduct {

        @Test
        @DisplayName("Xóa thành công")
        void whenProductExists_thenDelete() {
            when(productRepository.existsById(1L)).thenReturn(true);

            productService.deleteProduct(1L);

            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Xóa sản phẩm không tồn tại → ném exception")
        void whenProductNotFound_thenThrow() {
            when(productRepository.existsById(999L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.deleteProduct(999L));

            verify(productRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("addProduct()")
    class AddProduct {

        @Test
        @DisplayName("Thêm sản phẩm thành công")
        void whenValidRequest_thenSaveAndReturn() {
            // Arrange
            ProductRequest request = ProductRequest.builder()
                    .name("iPhone 16")
                    .price(new BigDecimal("25000000.00"))
                    .category_id(1L)
                    .build();

            Product newProduct = Product.builder()
                    .name("iPhone 16")
                    .price(new BigDecimal("25000000.00"))
                    .build();

            Product savedProduct = Product.builder()
                    .id(1L)
                    .name("iPhone 16")
                    .price(new BigDecimal("25000000.00"))
                    .category(category)
                    .build();

            when(categoryRepository.findById(1L))
                    .thenReturn(Optional.of(category));
            when(productMapper.toEntity(request))
                    .thenReturn(newProduct);
            when(productRepository.save(newProduct))
                    .thenReturn(savedProduct);
            when(productMapper.toResponse(savedProduct))
                    .thenReturn(productResponse);

            // Act
            ProductResponse result = productService.addProduct(request);

            // Assert
            assertNotNull(result);
            assertEquals("iPhone 16", result.getName());

            // Verify — đảm bảo audit log được ghi
            verify(auditService, times(1)).add(any());
            verify(productRepository).save(newProduct);
        }

        @Test
        @DisplayName("Category không tồn tại → ném exception")
        void whenCategoryNotFound_thenThrow() {
            ProductRequest request = ProductRequest.builder()
                    .name("Test")
                    .price(new BigDecimal("5000"))
                    .category_id(999L)
                    .build();

            when(categoryRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.addProduct(request));

            verify(productRepository, never()).save(any());
        }
    }
}
```

### 🧠 Phân tích kỹ pattern AAA

Mỗi test method đều theo **AAA pattern** — đây là tiêu chuẩn viết test:

```
Arrange  → Chuẩn bị dữ liệu, setup mock behavior
Act      → Gọi method đang test
Assert   → Kiểm tra kết quả + verify mock interaction
```

### 🧠 @Nested là gì?

`@Nested` nhóm các test case theo method. Khi chạy, kết quả sẽ hiện:

```
ProductServiceTest
├── getProductById()
│   ├── ✅ Tìm thấy sản phẩm → trả về ProductResponse
│   └── ✅ Không tìm thấy → ném ResourceNotFoundException
├── getProducts()
│   ├── ✅ Có sản phẩm → trả về danh sách
│   └── ✅ Không có sản phẩm → trả về list rỗng
├── deleteProduct()
│   ├── ✅ Xóa thành công
│   └── ✅ Xóa sản phẩm không tồn tại → ném exception
└── addProduct()
    ├── ✅ Thêm sản phẩm thành công
    └── ✅ Category không tồn tại → ném exception
```

---

## 6. @WebMvcTest — Test Controller Layer

### 6.1. @WebMvcTest là gì?

```
@WebMvcTest = khởi động Spring MÔT PHẦN
            → chỉ load: Controller + Filter + ControllerAdvice
            → KHÔNG load: Service, Repository, DB
            → Tự động có MockMvc để gửi HTTP request giả
```

> ⚠️ `ProductController` của bạn đang bị comment hết. Phần này dạy concept — khi bạn uncomment Controller, bạn sẽ viết test tương tự.

### 6.2. Ví dụ test Controller

```java
package com.ducknife.project.modules.product;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.ducknife.project.modules.product.dto.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)  // ← Chỉ load controller này
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;  // ← Gửi HTTP request giả (không cần server thật)

    @Autowired
    private ObjectMapper objectMapper;  // ← Convert object ↔ JSON

    @MockBean  // ← Mock Service (vì @WebMvcTest không load @Service)
    private ProductService productService;

    @Test
    @DisplayName("GET /api/products → 200 OK + danh sách sản phẩm")
    @WithMockUser  // ← Giả lập user đã đăng nhập (vì project có Spring Security)
    void showProducts_shouldReturn200() throws Exception {
        // Arrange — giả lập service trả về data
        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("iPhone 16")
                .price(new BigDecimal("25000000.00"))
                .build();

        given(productService.getProducts())
                .willReturn(List.of(response));

        // Act & Assert — gửi GET request và kiểm tra response
        mockMvc.perform(get("/api/products"))
                .andDo(print())  // in ra request/response để debug
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("iPhone 16"))
                .andExpect(jsonPath("$.data[0].price").value(25000000.00));
    }

    @Test
    @DisplayName("GET /api/products/1 → 200 OK + chi tiết sản phẩm")
    @WithMockUser
    void showProductById_shouldReturn200() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("iPhone 16")
                .price(new BigDecimal("25000000.00"))
                .build();

        given(productService.getProductById(1L))
                .willReturn(response);

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("iPhone 16"));
    }

    @Test
    @DisplayName("DELETE /api/products/1 — không có quyền ADMIN → 403 Forbidden")
    @WithMockUser(roles = "USER")  // ← User thường, không phải ADMIN
    void deleteProduct_withoutAdminRole_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/products/1 — có quyền ADMIN → 204 No Content")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_withAdminRole_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/products — body invalid → 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    void addProduct_invalidBody_shouldReturn400() throws Exception {
        // Gửi JSON thiếu name (vi phạm @NotBlank)
        String invalidJson = """
                {
                    "name": "",
                    "price": 500,
                    "category_id": 1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
```

### 6.3. Giải thích từng annotation

| Annotation | Mô tả |
|-----------|--------|
| `@WebMvcTest(Controller.class)` | Chỉ load controller được chỉ định |
| `@MockBean` | Tạo mock và đăng ký vào Spring Context (khác với `@Mock` của Mockito) |
| `@WithMockUser` | Giả lập user đã authenticate — bắt buộc khi có Spring Security |
| `@WithMockUser(roles = "ADMIN")` | Giả lập user với role ADMIN |
| `given(...).willReturn(...)` | BDD style của Mockito (đọc tự nhiên hơn when/thenReturn) |

### 6.4. MockMvc API

```java
// ① Gửi request
mockMvc.perform(
    get("/api/products")                          // GET
    post("/api/products")                         // POST
    put("/api/products/{id}", 1L)                 // PUT
    delete("/api/products/{id}", 1L)              // DELETE
        .contentType(MediaType.APPLICATION_JSON)  // Header
        .content(jsonString)                      // Body
        .header("Authorization", "Bearer xxx")    // Custom header
)

// ② Kiểm tra response
.andExpect(status().isOk())           // 200
.andExpect(status().isCreated())      // 201
.andExpect(status().isNoContent())    // 204
.andExpect(status().isBadRequest())   // 400
.andExpect(status().isForbidden())    // 403
.andExpect(status().isNotFound())     // 404

// ③ Kiểm tra JSON body
.andExpect(jsonPath("$.data.name").value("iPhone 16"))
.andExpect(jsonPath("$.data").isArray())
.andExpect(jsonPath("$.data", hasSize(3)))

// ④ Debug
.andDo(print())  // In ra request + response đầy đủ
```

---

## 7. @DataJpaTest — Test Repository Layer

### 7.1. @DataJpaTest là gì?

```
@DataJpaTest = khởi động Spring MỘT PHẦN
             → chỉ load: JPA, Hibernate, Repository
             → tự động dùng H2 in-memory database (nếu có)
             → mỗi test method tự rollback (không bẩn data)
             → KHÔNG load: Controller, Service, Security
```

### 7.2. Ví dụ test Repository

```java
package com.ducknife.project.modules.product;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.ducknife.project.modules.category.Category;

@DataJpaTest  // ← Load JPA + H2 in-memory DB
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;  // ← Để insert data test trực tiếp

    private Category category;

    @BeforeEach
    void setUp() {
        // Tạo category trước (vì Product cần category)
        category = Category.builder()
                .name("Điện thoại")
                .build();
        entityManager.persistAndFlush(category);
    }

    @Test
    @DisplayName("existsByName() → true khi sản phẩm tồn tại")
    void existsByName_whenExists_thenTrue() {
        // Arrange — insert product qua EntityManager
        Product product = Product.builder()
                .name("iPhone 16")
                .price(new BigDecimal("25000000.00"))
                .category(category)
                .build();
        entityManager.persistAndFlush(product);

        // Act
        Boolean exists = productRepository.existsByName("iPhone 16");

        // Assert — dùng AssertJ (đọc tự nhiên hơn JUnit assertions)
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByName() → false khi không tồn tại")
    void existsByName_whenNotExists_thenFalse() {
        Boolean exists = productRepository.existsByName("Galaxy S25");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByCategoryId() → trả về đúng products của category")
    void findByCategoryId_shouldReturnCorrectProducts() {
        Product p1 = Product.builder()
                .name("iPhone 16").price(new BigDecimal("25000000")).category(category).build();
        Product p2 = Product.builder()
                .name("iPhone 15").price(new BigDecimal("20000000")).category(category).build();
        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.flush();

        List<Product> products = productRepository.findByCategoryId(category.getId());

        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName)
                .containsExactlyInAnyOrder("iPhone 16", "iPhone 15");
    }

    @Test
    @DisplayName("findByNameAndPrice() → lọc đúng theo tên và khoảng giá")
    void findByNameAndPrice_shouldFilterCorrectly() {
        Product p1 = Product.builder()
                .name("iPhone 16").price(new BigDecimal("25000000")).category(category).build();
        entityManager.persistAndFlush(p1);

        List<Product> results = productRepository.findByNameAndPrice(
                "iPhone 16", 20000000, 30000000);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("iPhone 16");
    }

    @Test
    @DisplayName("save() → lưu product mới với id tự tăng")
    void save_shouldPersistAndGenerateId() {
        Product product = Product.builder()
                .name("Galaxy S25")
                .price(new BigDecimal("22000000"))
                .category(category)
                .build();

        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Galaxy S25");

        // Verify trong DB thật
        Product found = entityManager.find(Product.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Galaxy S25");
    }
}
```

### 7.3. AssertJ vs JUnit Assertions

```java
// JUnit (cũ, vẫn dùng được)
assertEquals("iPhone 16", product.getName());
assertTrue(list.size() > 0);

// AssertJ (mới, đọc tự nhiên hơn, IDE gợi ý tốt hơn)
assertThat(product.getName()).isEqualTo("iPhone 16");
assertThat(list).isNotEmpty();
assertThat(list).hasSize(2);
assertThat(list).extracting(Product::getName)
                .containsExactlyInAnyOrder("iPhone 16", "Galaxy S25");
```

> 💡 **Best practice**: Dùng **AssertJ** cho Integration Test, **JUnit** cho Unit Test đơn giản.
> Spring Boot đã include sẵn AssertJ — không cần thêm dependency.

### 7.4. Vấn đề với H2 In-Memory

```
⚠️ H2 ≠ MySQL
   → Một số query/feature khác biệt giữa H2 và MySQL
   → Test pass trên H2 có thể fail trên MySQL thật
   → Giải pháp: Testcontainers (phần tiếp theo)
```

---

## 8. Testcontainers — Test với DB thật

### 8.1. Testcontainers là gì?

```
Testcontainers = Thư viện Java tự động:
  1. Kéo Docker image MySQL
  2. Khởi động container MySQL
  3. Chạy test trên MySQL THẬT
  4. Tắt container sau khi test xong

→ Test chính xác 100% với DB production
→ Không cần cài MySQL trên máy
→ CHỈ CẦN Docker đang chạy
```

### 8.2. Thêm dependency

Thêm vào `pom.xml`:

```xml
<!-- Testcontainers BOM — quản lý version -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-bom</artifactId>
            <version>1.20.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- Trong <dependencies> -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
```

### 8.3. Cấu hình `application-test.yml`

Tạo file `src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    # Testcontainers tự điền URL, username, password
    # tc: prefix = Testcontainers JDBC driver
    url: jdbc:tc:mysql:8.0:///testdb
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    hibernate:
      ddl-auto: create-drop  # Tự tạo schema từ Entity, xóa sau khi test
    show-sql: true
  flyway:
    enabled: false  # Tắt Flyway khi test (để JPA tự tạo bảng)
```

### 8.4. Base class cho Integration Test

```java
package com.ducknife.project;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container  // Testcontainers tự start/stop container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource  // Truyền thông tin container vào Spring
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
```

### 8.5. Integration Test với Testcontainers

```java
package com.ducknife.project.modules.product;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.AbstractIntegrationTest;
import com.ducknife.project.modules.category.Category;
import com.ducknife.project.modules.category.CategoryRepository;

@Transactional  // Rollback sau mỗi test
class ProductIntegrationTest extends AbstractIntegrationTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Lưu và tìm product trên MySQL thật")
    void saveAndFind_onRealMySQL() {
        // Arrange
        Category category = categoryRepository.save(
            Category.builder().name("Laptop").build()
        );

        Product product = Product.builder()
                .name("MacBook Pro M4")
                .price(new BigDecimal("45000000.00"))
                .category(category)
                .build();

        // Act
        Product saved = productRepository.save(product);

        // Assert — data nằm trong MySQL container thật!
        assertThat(saved.getId()).isNotNull();

        Product found = productRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("MacBook Pro M4");
        assertThat(found.getPrice()).isEqualByComparingTo("45000000.00");
        assertThat(found.getCategory().getName()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("existsByName() hoạt động đúng trên MySQL")
    void existsByName_onRealMySQL() {
        Category category = categoryRepository.save(
            Category.builder().name("Phone").build()
        );
        productRepository.save(
            Product.builder().name("Pixel 9").price(new BigDecimal("18000000"))
                    .category(category).build()
        );

        assertThat(productRepository.existsByName("Pixel 9")).isTrue();
        assertThat(productRepository.existsByName("Nokia 3310")).isFalse();
    }
}
```

### 8.6. So sánh H2 vs Testcontainers

| Tiêu chí | H2 In-Memory | Testcontainers MySQL |
|----------|-------------|---------------------|
| **Tốc độ** | ⚡ Rất nhanh (< 1s) | 🐢 Chậm hơn (5-15s lần đầu) |
| **Chính xác** | ⚠️ Có thể khác MySQL | ✅ 100% giống production |
| **Cần Docker** | ❌ Không | ✅ Bắt buộc |
| **Khi nào dùng** | Prototype, learning | **Production, CI/CD** |
| **2026 trend** | Đang bị thay thế | **Tiêu chuẩn ngành** |

---

## 9. Tổng kết & Best Practice

### 9.1. Bảng tổng kết — Khi nào dùng gì?

| Layer | Annotation | Mock/Real | Tốc độ | Khi nào dùng |
|-------|-----------|-----------|--------|-------------|
| **Service** | `@ExtendWith(MockitoExtension)` | Mock hết dependency | ⚡ Cực nhanh | **Luôn luôn viết** |
| **Controller** | `@WebMvcTest` | Mock Service | 🟡 Nhanh | Khi cần test HTTP + Security |
| **Repository** | `@DataJpaTest` | H2 DB | 🟡 Nhanh | Khi test custom query |
| **Full stack** | `@SpringBootTest + Testcontainers` | MySQL thật | 🔴 Chậm | CI/CD, test flow phức tạp |

### 9.2. Cấu trúc thư mục test khuyến nghị

```
src/test/java/com/ducknife/project/
├── AbstractIntegrationTest.java        ← Base class cho Testcontainers
├── ProjectApplicationTests.java        ← Spring context load test
├── unit/                               ← Pure unit test (không cần Spring)
│   ├── ShippingFeeCalculatorTest.java  ← (Bạn đã có ✅)
│   └── OrderTest.java                 ← (Bạn đã có ✅)
└── modules/
    ├── product/
    │   ├── ProductServiceTest.java     ← Unit test với Mockito
    │   ├── ProductControllerTest.java  ← @WebMvcTest
    │   ├── ProductRepositoryTest.java  ← @DataJpaTest
    │   └── ProductIntegrationTest.java ← @SpringBootTest + Testcontainers
    ├── category/
    │   └── ...
    └── order/
        └── ...
```

### 9.3. Naming Convention

```java
// Pattern: methodName_condition_expectedResult
void getProductById_whenProductExists_thenReturnResponse()
void getProductById_whenProductNotFound_thenThrowException()
void deleteProduct_withoutAdminRole_shouldReturn403()

// Hoặc dùng @DisplayName cho tiếng Việt
@DisplayName("Tìm thấy sản phẩm → trả về ProductResponse")
void whenProductExists_thenReturnResponse()
```

### 9.4. Checklist trước khi commit

- [ ] Mỗi Service method có ít nhất 2 test: happy path + error path
- [ ] Test exception message, không chỉ exception type
- [ ] Verify mock interaction (method được gọi / KHÔNG được gọi)
- [ ] Không dùng `@SpringBootTest` cho Unit Test (chậm vô ích)
- [ ] `@DisplayName` mô tả rõ ràng cho mọi test method
- [ ] Test chạy **độc lập** — không phụ thuộc thứ tự chạy

### 9.5. Chạy test

```bash
# Chạy tất cả test
./mvnw test

# Chạy 1 class test cụ thể
./mvnw test -Dtest=ProductServiceTest

# Chạy 1 method test cụ thể
./mvnw test -Dtest=ProductServiceTest#whenProductExists_thenReturnResponse

# Chạy test và hiện kết quả chi tiết
./mvnw test -Dsurefire.useFile=false
```

---

## 📝 Bài tập thực hành

> Áp dụng từng bước vào project `com.ducknife.project`

| # | Bài tập | Độ khó |
|---|---------|--------|
| 1 | Tạo `ProductServiceTest` — test `getProductById()`, `getProducts()`, `deleteProduct()` | ⭐ |
| 2 | Thêm test `addProduct()` — cả happy path lẫn category not found | ⭐⭐ |
| 3 | Tạo `ProductRepositoryTest` với `@DataJpaTest` — test `existsByName()`, `findByCategoryId()` | ⭐⭐ |
| 4 | Uncomment `ProductController` → viết `ProductControllerTest` với `@WebMvcTest` | ⭐⭐⭐ |
| 5 | Setup Testcontainers + `AbstractIntegrationTest` → test trên MySQL thật | ⭐⭐⭐ |
