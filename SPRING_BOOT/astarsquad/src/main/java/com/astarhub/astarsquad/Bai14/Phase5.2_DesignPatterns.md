# 🏗️ Phase 5.2: Design Patterns Trong Spring Boot

---

## 📑 Mục Lục

- [1. Tổng Quan Design Patterns](#1-tổng-quan-design-patterns)
- [2. DTO Pattern](#2-dto-pattern)
  - [2.1 DTO Là Gì?](#21-dto-là-gì)
  - [2.2 Mapping Entity ↔ DTO](#22-mapping-entity--dto)
  - [2.3 MapStruct — Auto Mapping](#23-mapstruct--auto-mapping)
- [3. Repository Pattern](#3-repository-pattern)
- [4. Service Layer Pattern](#4-service-layer-pattern)
- [5. Builder Pattern](#5-builder-pattern)
- [6. Factory Pattern](#6-factory-pattern)
- [7. Strategy Pattern](#7-strategy-pattern)
- [7.5 Factory vs Strategy — Khác Nhau Chỗ Nào?](#75-factory-vs-strategy--khác-nhau-chỗ-nào)
- [8. Tổng Kết: Patterns Trong Kiến Trúc Spring](#8-tổng-kết-patterns-trong-kiến-trúc-spring)
- [9. Pattern Spring Dùng LÊN Bạn (Proxy & Template Method)](#9-pattern-spring-dùng-lên-bạn-proxy--template-method)
- [✅ Checklist](#-checklist)

---

## 1. Tổng Quan Design Patterns

**Design Pattern** = giải pháp tái sử dụng cho các vấn đề phổ biến trong thiết kế phần mềm.

```
Trong Spring Boot, bạn ĐÃ dùng nhiều pattern mà không biết:

@RestController  → Controller Pattern
@Service         → Service Layer Pattern
@Repository      → Repository Pattern
@Builder         → Builder Pattern
DI/IoC           → Dependency Injection Pattern
SecurityFilterChain → Chain of Responsibility Pattern
```

### ⚠️ Phân biệt: Design Pattern (GoF) vs Quy ước kiến trúc

Bài này gộp hai loại khác nhau vào một chỗ — cần tách rõ để không nhầm:

| Nội dung trong bài | Thực chất là gì |
|---|---|
| **Builder, Factory, Strategy** | ✅ Design Pattern đúng nghĩa (Gang of Four, 1994) |
| **DTO, Repository, Service Layer** | ⚙️ **Quy ước phân tầng** của Spring/enterprise — không nằm trong 23 pattern GoF |

Cả hai đều đáng học, nhưng khác bản chất: GoF pattern là **giải pháp cho vấn đề thiết kế object**, còn quy ước phân tầng là **cách tổ chức thư mục và trách nhiệm** trong ứng dụng web. Nói "DTO là design pattern" trong phỏng vấn thì không sai lắm, nhưng người hỏi kỹ sẽ truy tiếp.

---

## 2. DTO Pattern

### 2.1 DTO Là Gì?

**DTO (Data Transfer Object)** = object chỉ chứa data, dùng để truyền giữa các tầng.

```
Không có DTO:
Client ←→ Controller ←→ Service ←→ Repository
              ↑
          Entity trực tiếp
          → Lộ password, internal fields
          → Circular reference (Entity có relationship)
          → Client thay đổi → Entity thay đổi

Có DTO:
Client ←→ [RequestDTO] ←→ Controller ←→ Service ←→ Repository
Client ←→ [ResponseDTO] ←→                          ↕
                                                   Entity
→ Client chỉ thấy fields cần thiết
→ Entity thay đổi → DTO giữ nguyên (backward compatible)
```

### 2.2 Mapping Entity ↔ DTO

```java
// ===== Entity =====
@Entity
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;  // KHÔNG được lộ ra ngoài!
    private boolean active;
    private Set<Role> roles;
}

// ===== Response DTO — chỉ chứa data client cần =====
public record UserResponse(
    Long id,
    String username,
    String email,
    List<String> roles
) {
    // Factory method: Entity → DTO
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRoles().stream()
                .map(Role::getName).toList()
        );
        // password KHÔNG có ở đây!
    }
}

// ===== Request DTO — validate input =====
public record CreateUserRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @Email String email,
    @NotBlank @Size(min = 8) String password
) {}

// ===== Service =====
@Service
public class UserService {
    public UserResponse createUser(CreateUserRequest req) {
        User user = User.builder()
            .username(req.username())
            .email(req.email())
            .password(passwordEncoder.encode(req.password()))
            .build();
        return UserResponse.from(userRepository.save(user));
    }
}
```

> **`UserResponse.from(user)` là _static factory method_** (Effective Java, Item 1) — **KHÔNG** phải Factory Pattern của GoF. Trùng chữ "factory" nhưng khác hẳn: static factory method chỉ là cách thay thế `new`, còn Factory Pattern là chọn **implementation nào** được tạo lúc runtime.

### 2.2b Dùng `record` cho DTO — có điều kiện

| Loại | record? | Lý do |
|---|---|---|
| **Response DTO** | ✅ nên dùng | Bất biến, gọn, có sẵn `equals`/`hashCode`/`toString` |
| **Request DTO** | ✅ dùng được | Jackson + Bean Validation hỗ trợ đầy đủ. Đánh đổi: mất `@Builder` tiện tay khi viết test |
| **Entity** | ❌ **TUYỆT ĐỐI KHÔNG** | JPA bắt buộc constructor rỗng, class không `final`, field mutable để proxy & lazy loading hoạt động. Record vi phạm cả ba |

### 2.3 MapStruct — Auto Mapping

> ⚠️ **Cấu hình dưới đây là cách CŨ và sẽ KHÔNG hoạt động nếu `pom.xml` đã dùng `annotationProcessorPaths`** (mặc định khi có Lombok). Xem mục 2.3b cho cấu hình đúng.

```xml
<!-- pom.xml — CÁCH CŨ, chỉ đúng khi KHÔNG dùng annotationProcessorPaths -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(CreateUserRequest request);
    
    // Custom mapping
    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(Role::getName).toList())")
    UserResponse toDetailResponse(User user);
}

// Sử dụng:
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;  // Inject mapper

    public UserResponse createUser(CreateUserRequest req) {
        User user = userMapper.toEntity(req);
        return userMapper.toResponse(userRepository.save(user));
    }
}
```

### 2.3b ⚠️ MapStruct + Lombok — cái bẫy làm mapper RỖNG

Khi dự án đã có Lombok, `pom.xml` thường khai annotation processor qua `annotationProcessorPaths`. Lúc đó Maven **bỏ qua hoàn toàn** processor nằm trong `<dependencies>` — cấu hình ở mục 2.3 sẽ không có tác dụng.

Tệ hơn, nếu thiếu **`lombok-mapstruct-binding`** thì:

```
MapStruct chạy TRƯỚC khi Lombok sinh getter/setter
    → MapStruct nhìn vào class, thấy KHÔNG có getter nào
    → sinh ra mapper RỖNG (mọi field = null)
    → compile THÀNH CÔNG, chạy KHÔNG lỗi
    → phát hiện ra lúc test/production
```

Đây là lỗi **fail silently** — nguy hiểm hơn lỗi đỏ nhiều, vì không có gì báo cho bạn biết.

**Cấu hình đúng** — cả 3 path trong CÙNG một khối, đúng thứ tự này:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </path>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Trong `<dependencies>` chỉ cần `org.mapstruct:mapstruct` (thư viện runtime), **không** cần `mapstruct-processor`.

> **Version**: `1.5.5.Final` trong bài là bản 2023. Kiểm tra bản mới nhất trên Maven Central trước khi dùng.

### 2.3c Có nên dùng MapStruct không?

Đây là quyết định thật, không có đáp án duy nhất:

| | Viết tay `from()` | MapStruct |
|---|---|---|
| Đọc & debug | Thấy rõ từng dòng, đặt breakpoint được | Phải mở file generate trong `target/` |
| Khi đổi field | Compiler báo lỗi ngay | Có thể âm thầm bỏ qua field mới |
| Số lượng DTO ít (< 10) | ✅ Gọn hơn | Thêm phức tạp build không đáng |
| Số lượng DTO nhiều (30+) | Lặp code mệt mỏi | ✅ Thắng rõ rệt |

**Khuyến nghị**: làm một lần cho biết (đi làm chắc chắn gặp), sau đó tự quyết định giữ hay bỏ theo quy mô dự án.

---

## 3. Repository Pattern

> **Đã dùng** từ Phase 3 (JpaRepository). Tóm tắt:

```
Repository Pattern tách biệt business logic khỏi data access logic.

Service → Repository (interface) → JPA Implementation → Database
          ↑ Service chỉ biết interface
          ↑ Không biết SQL, không biết database nào
```

```java
// Spring Data JPA tự generate implementation
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
}
```

**Custom Repository** khi cần logic phức tạp:

```java
// Interface
public interface UserRepositoryCustom {
    List<User> searchUsers(UserSearchCriteria criteria);
}

// Implementation (Spring tự detect bằng tên "Impl")
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<User> searchUsers(UserSearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        // ... dynamic query
    }
}

// Kế thừa cả hai
public interface UserRepository 
    extends JpaRepository<User, Long>, UserRepositoryCustom {}
```

---

## 4. Service Layer Pattern

```
Controller (nhận request, validate, trả response)
     │
     ▼
Service (business logic, transaction, orchestration)
     │
     ▼
Repository (data access)
```

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Default readonly cho GET
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OrderMapper orderMapper;

    // Business logic = orchestrate nhiều repository + service
    @Transactional  // Override: cần write
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {
        // 1. Validate business rules
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        // 2. Business logic
        Order order = orderMapper.toEntity(request);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNumber(generateOrderNumber());

        // 3. Save
        Order saved = orderRepository.save(order);

        // 4. Side effects
        notificationService.sendOrderConfirmation(saved);

        // 5. Return DTO
        return orderMapper.toResponse(saved);
    }
}
```

---

## 5. Builder Pattern

> **Đã dùng** với Lombok `@Builder`. Giải thích bên trong:

```java
// Lombok @Builder tự generate code này:
public class User {
    private Long id;
    private String username;
    private String email;

    // Builder inner class
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String email;

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder username(String u) { this.username = u; return this; }
        public UserBuilder email(String e) { this.email = e; return this; }

        public User build() {
            return new User(id, username, email);
        }
    }
}

// Sử dụng — fluent API:
User user = User.builder()
    .username("admin")
    .email("admin@example.com")
    .build();
```

---

## 6. Factory Pattern

### 6.1 ❌ Cách tra theo tên bean — CHẠY ĐƯỢC nhưng mong manh

```java
@Component
@RequiredArgsConstructor
public class NotificationFactory {

    private final Map<String, NotificationSender> senders;
    // Spring inject tất cả bean implement NotificationSender
    // Key = TÊN BEAN, Value = instance

    public NotificationSender getSender(NotificationType type) {
        String beanName = type.name().toLowerCase() + "NotificationSender";  // ⚠️ ghép chuỗi
        NotificationSender sender = senders.get(beanName);
        if (sender == null) throw new IllegalArgumentException(
            "Unsupported notification type: " + type);
        return sender;
    }
}

@Component("emailNotificationSender")   // tên bean PHẢI khớp chính xác công thức trên
public class EmailNotificationSender implements NotificationSender { ... }
```

Việc Spring inject `Map<String, T>` với key là tên bean là **tính năng thật**, không sai. Nhưng cách ghép chuỗi để tra cứu có 3 điểm yếu:

- Đổi tên bean hoặc đổi tên hằng enum → **vỡ lúc runtime**, compiler không cản được
- Thêm implementation mới mà quên đặt đúng tên bean → không ai biết cho tới khi user gặp lỗi
- Đọc code không biết `NotificationType.EMAIL` sẽ đi tới class nào, phải suy luận qua công thức chuỗi

### 6.2 ✅ Cách type-safe — để implementation tự khai báo

```java
public interface NotificationSender {
    NotificationType getType();                    // ← tự khai, không đoán qua tên bean
    void send(String to, String message);
}

@Component
public class EmailNotificationSender implements NotificationSender {
    public NotificationType getType() { return NotificationType.EMAIL; }
    public void send(String to, String message) { /* email logic */ }
}

@Component
public class SmsNotificationSender implements NotificationSender {
    public NotificationType getType() { return NotificationType.SMS; }
    public void send(String to, String message) { /* sms logic */ }
}

@Component
public class NotificationFactory {

    private final Map<NotificationType, NotificationSender> senders;

    public NotificationFactory(List<NotificationSender> list) {
        this.senders = list.stream()
                .collect(Collectors.toMap(NotificationSender::getType, Function.identity()));
    }

    public NotificationSender get(NotificationType type) {
        NotificationSender sender = senders.get(type);
        if (sender == null) {
            throw new InvalidRequestException("Chưa hỗ trợ loại thông báo: " + type);
        }
        return sender;
    }
}
```

**Ba cái lợi:**

1. **Compiler bắt lỗi** — key là enum, gõ sai không compile được
2. **Trùng lặp bị phát hiện lúc KHỞI ĐỘNG** — `Collectors.toMap` ném `IllegalStateException` nếu hai bean cùng khai một type, thay vì âm thầm ghi đè nhau
3. **Thêm provider mới = thêm 1 class**, không cần sửa factory (nguyên tắc Open/Closed)

> **Ném exception gì?** Đừng dùng `IllegalArgumentException` trần — nó rơi vào `@ExceptionHandler(Exception.class)` và trả **500**, trong khi bản chất là client gửi tham số sai → phải **400**. Dùng exception nghiệp vụ của dự án (`InvalidRequestException` kế thừa `AppException`).

---

## 7. Strategy Pattern

```java
// Interface chiến lược
public interface PricingStrategy {
    BigDecimal calculatePrice(Order order);
    boolean supports(CustomerType type);
}

@Component
public class RegularPricing implements PricingStrategy {
    public BigDecimal calculatePrice(Order order) { return order.getSubtotal(); }
    public boolean supports(CustomerType type) { return type == CustomerType.REGULAR; }
}

@Component
public class VipPricing implements PricingStrategy {
    public BigDecimal calculatePrice(Order order) {
        return order.getSubtotal().multiply(BigDecimal.valueOf(0.9)); // giảm 10%
    }
    public boolean supports(CustomerType type) { return type == CustomerType.VIP; }
}

// Service chọn strategy
@Service
@RequiredArgsConstructor
public class PricingService {
    private final List<PricingStrategy> strategies; // Spring inject tất cả

    public BigDecimal calculatePrice(Order order, CustomerType type) {
        return strategies.stream()
            .filter(s -> s.supports(type))
            .findFirst()
            .orElseThrow(() -> new InvalidRequestException(   // ⚠️ KHÔNG dùng orElseThrow() trần
                    "Không hỗ trợ loại khách hàng: " + type))
            .calculatePrice(order);
    }
}
```

### ⚠️ Hai điểm phải sửa so với bản gốc

**1. `orElseThrow()` trần → lỗi 500 giả.** Không truyền gì vào thì nó ném `NoSuchElementException` — không message, rơi thẳng vào `@ExceptionHandler(Exception.class)` và trả **500 "Lỗi hệ thống"**. Nhưng bản chất đây là client gửi loại khách hàng không hợp lệ → phải là **400**. Luôn truyền exception nghiệp vụ có message rõ ràng.

**2. `stream().filter().findFirst()` quét lại danh sách MỖI lần gọi.** Với 2-3 strategy thì không đáng kể. Nhưng nếu số lượng lớn hoặc method được gọi trong vòng lặp, dựng sẵn `Map` trong constructor (như Factory ở mục 6.2) cho tra cứu O(1):

```java
public PricingService(List<PricingStrategy> list) {
    this.strategies = list.stream()
            .collect(Collectors.toMap(PricingStrategy::getCustomerType, Function.identity()));
}
```

Đánh đổi: `supports()` linh hoạt hơn (điều kiện phức tạp, nhiều tiêu chí), `Map` nhanh hơn nhưng chỉ tra được theo một khóa duy nhất. Chọn theo nhu cầu thật.

---

## 7.5 Factory vs Strategy — Khác Nhau Chỗ Nào?

Đọc mục 6 và 7 sẽ thấy code **gần như giống hệt nhau**: đều là "có nhiều implementation, chọn một theo tham số". Khác biệt nằm ở **ý định**, không ở cấu trúc code.

| | **Factory** | **Strategy** |
|---|---|---|
| Chọn cái gì | **Đối tượng nào được TẠO RA** | **Thuật toán nào được CHẠY** |
| Trả về | Một instance để bạn dùng tiếp | Kết quả tính toán |
| Nếu bỏ pattern đi, code thành gì | `new EmailSender()` rải rác khắp nơi | `if/else` dài chứa logic tính toán |
| Câu hỏi nó trả lời | "Tạo cái gì?" | "Làm thế nào?" |

**Phép thử nhanh:** tự hỏi *"nếu xóa pattern này, cái gì mọc lên thay thế?"*
- Mọc ra `new` → đó là **Factory**
- Mọc ra `if/else` chứa công thức → đó là **Strategy**

Trong thực tế hai pattern hay đi cùng nhau: Factory tạo ra Strategy phù hợp, rồi Strategy thực thi.

---

## 8. Tổng Kết: Patterns Trong Kiến Trúc Spring

```
Client
  │
  ▼
Controller          → DTO Pattern (Request/Response DTO)
  │
  ▼
Service             → Service Layer, Strategy, Factory
  │
  ▼
Repository          → Repository Pattern
  │
  ▼
Entity/Database     → Builder Pattern
```

---

## 9. Pattern Spring Dùng LÊN Bạn (Proxy & Template Method)

> Mục 2–7 là pattern **bạn viết**. Mục này là pattern **Spring áp dụng lên code của bạn** — bạn dùng nó mỗi ngày mà thường không nhận ra. Với người đi làm, hiểu hai pattern này giá trị hơn Factory/Strategy nhiều, vì chúng giải thích *vì sao Spring hoạt động được*.

### 9.1 Proxy Pattern — linh hồn của mọi annotation

```java
@Service
public class ProductService {
    @Transactional
    public ProductResponse addProduct(...) { ... }
}
```

Spring **không** đưa `ProductService` thật cho bạn. Nó bọc một lớp proxy bên ngoài:

```
Controller gọi productService.addProduct()
        │
        ▼
   ProductService$$SpringCGLIB          ← PROXY, không phải class bạn viết
        │  1. mở transaction
        │  2. gọi method THẬT ────────► ProductService (của bạn)
        │  3. commit / rollback
        ▼
     trả kết quả
```

`@Transactional`, `@PreAuthorize`, `@Async`, `@Cacheable`, `@Retryable` — **tất cả** chạy bằng cơ chế này.

#### ⚠️ Cái bẫy kinh điển: self-invocation

```java
@Service
public class OrderService {

    public void processOrder() {
        saveAudit();          // ❌ gọi nội bộ → KHÔNG qua proxy → @Transactional VÔ HIỆU
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAudit() { ... }
}
```

Gọi `this.saveAudit()` là gọi thẳng method thật, proxy **không hề biết** → annotation bị bỏ qua hoàn toàn. Không lỗi, không cảnh báo, transaction đơn giản là không tồn tại.

Lại là **fail silently** — cùng họ với mapper rỗng ở mục 2.3b.

**Cách tránh:** tách method có annotation sang **bean khác** rồi inject vào (cách sạch nhất), hoặc tự inject chính mình qua `@Lazy` (cách xấu, chỉ dùng khi cùng đường).

#### Hệ quả khác của Proxy

| Điều | Vì sao |
|---|---|
| Method `private`/`final` không nhận annotation | Proxy không override được |
| Class `final` không proxy được bằng CGLIB | Không kế thừa được |
| Annotation trên method gọi từ constructor không chạy | Proxy chưa dựng xong |

### 9.2 Template Method — khung cố định, chừa lỗ cho bạn điền

Bạn **đã dùng pattern này** ở Phase 5.1 khi kế thừa `ResponseEntityExceptionHandler`:

```java
public final ResponseEntity<Object> handleException(...)   ← TEMPLATE (final: khung bất biến)
    │   quyết định thứ tự, dispatch bằng instanceof
    ├──► protected handleMethodArgumentNotValid(...)       ← HOOK: bạn @Override
    ├──► protected handleHandlerMethodValidationException(...) ← HOOK
    └──► protected handleExceptionInternal(...)            ← HOOK
```

Lớp cha giữ **thuật toán tổng thể**, khóa lại bằng `final` để không ai phá. Lớp con chỉ được điền vào các "lỗ" đã định sẵn.

Đó cũng là lý do bạn **không thể** `@Override handleException` mà phải override các method `protected` bên dưới — và tại sao khai `@ExceptionHandler` trùng type với lớp cha lại gây lỗi `Ambiguous @ExceptionHandler method` lúc khởi động.

Spring dùng Template Method ở khắp nơi: `JdbcTemplate`, `RestTemplate`, `AbstractApplicationContext.refresh()`.

### 9.3 Các pattern GoF khác Spring dùng

| Pattern | Ở đâu trong Spring |
|---|---|
| **Singleton** | Bean scope mặc định |
| **Chain of Responsibility** | `SecurityFilterChain`, servlet `Filter` |
| **Adapter** | `HandlerAdapter` (nối DispatcherServlet với đủ loại controller) |
| **Observer** | `ApplicationEvent` + `@EventListener` |
| **Decorator** | `HttpServletRequestWrapper` |

---

## ✅ Checklist

**Quy ước phân tầng** (không phải GoF pattern, nhưng bắt buộc phải nắm):
- [ ] Dùng DTO tách biệt Entity và API contract
- [ ] Dùng `record` cho Response DTO — **KHÔNG BAO GIỜ** cho Entity
- [ ] Hiểu Repository Pattern + Custom Repository
- [ ] Service layer chứa business logic, không phải Controller

**Design Pattern bạn tự viết:**
- [ ] Builder Pattern với Lombok `@Builder`
- [ ] Factory Pattern — dùng key **type-safe**, không ghép chuỗi tên bean
- [ ] Strategy Pattern — ném exception nghiệp vụ, không `orElseThrow()` trần
- [ ] Phân biệt được Factory vs Strategy (tạo *cái gì* vs làm *thế nào*)

**Pattern Spring áp lên bạn** (quan trọng nhất khi đi làm):
- [ ] Hiểu Proxy — vì sao `@Transactional` hoạt động
- [ ] Tránh được bẫy self-invocation
- [ ] Nhận ra Template Method trong `ResponseEntityExceptionHandler`

**Công cụ (tùy chọn):**
- [ ] MapStruct — nhớ `lombok-mapstruct-binding`, nếu không mapper sẽ rỗng

---

> **Tiếp theo**: Đọc `Phase5.3_Testing.md` →
