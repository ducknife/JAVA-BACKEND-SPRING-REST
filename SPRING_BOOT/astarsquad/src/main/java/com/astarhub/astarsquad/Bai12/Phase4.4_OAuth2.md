# 🌐 OAuth2 — Social Login & Resource Server

> **Tích hợp từ**: File 1-3 (SecurityConfig + UserDetails + JWT)
> **File này tạo**: OAuth2UserService, OAuth2SuccessHandler, User entity mở rộng
> **Ref**: [OAuth2 Login](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/)

---

## 📑 Mục Lục

- [1. OAuth2 Là Gì?](#1-oauth2-là-gì)
  - [1.1 Các Vai Trò](#11-các-vai-trò)
  - [1.2 Authorization Code Flow](#12-authorization-code-flow--luồng-phổ-biến-nhất)
- [2. OAuth2 Login (Social Login)](#2-oauth2-login-social-login)
  - [2.1 Dependency](#21-dependency)
  - [2.2 application.yml](#22-applicationyml)
  - [2.3 User Entity — Mở Rộng Từ File 2](#23-user-entity--mở-rộng-từ-file-2)
  - [2.4 CustomOAuth2UserService](#24-customoauth2userservice--xử-lý-user-từ-provider)
  - [2.5 CustomOAuth2User](#25-customoauth2user)
  - [2.6 OAuth2 Login → Tạo JWT](#26-oauth2-login--tạo-jwt)
  - [2.7 SecurityConfig — Tích Hợp](#27-securityconfig--tích-hợp-oauth2-login)
- [3. OAuth2 Resource Server](#3-oauth2-resource-server--api-nhận-jwt-từ-bên-ngoài)
- [4. Khi Nào Dùng Gì?](#4-khi-nào-dùng-gì)
- [5. 🔒 Senior Review — Cạm Bẫy Thực Tế Khi Lên Production](#5--senior-review--cạm-bẫy-thực-tế-khi-lên-production)
- [✅ Checklist](#-checklist)

---

## 1. OAuth2 Là Gì?

**OAuth2** = giao thức ủy quyền (authorization framework) cho phép ứng dụng 
truy cập tài nguyên của user trên dịch vụ khác **mà không cần biết password**.

### 1.1 Các Vai Trò

| Vai trò | Là ai | Ví dụ |
|---------|-------|-------|
| **Resource Owner** | User — người sở hữu data | Bạn (có tài khoản Google) |
| **Client** | App muốn truy cập data của user | App của bạn (Spring Boot) |
| **Authorization Server** | Server cấp phát token | Google, GitHub, Keycloak |
| **Resource Server** | API bảo vệ data bằng token | API backend của bạn |

### 1.2 Authorization Code Flow — Luồng Phổ Biến Nhất

```
1. User click "Login with Google" trên app
        │
        ▼
2. App redirect user → Google Authorization Server
   https://accounts.google.com/o/oauth2/auth
   ?client_id=xxx            ← App ID đăng ký với Google
   &redirect_uri=http://localhost:8080/login/oauth2/code/google
   &scope=openid profile email
   &response_type=code       ← Yêu cầu authorization code
        │
        ▼
3. User đăng nhập Google + đồng ý chia sẻ thông tin
        │
        ▼
4. Google redirect về app kèm authorization code
   http://localhost:8080/login/oauth2/code/google?code=ABC123
        │
        ▼
5. Backend gửi code → Google để đổi lấy access_token
   (Server-to-server, KHÔNG qua browser → an toàn)
   POST https://oauth2.googleapis.com/token
   {code: "ABC123", client_id, client_secret}
        │
        ▼
6. Google trả về access_token
        │
        ▼
7. Backend dùng access_token gọi Google API lấy user info
   GET https://www.googleapis.com/oauth2/v3/userinfo
   → {email, name, picture}
        │
        ▼
8. Backend tạo/update user trong DB → tạo JWT nội bộ → trả cho client
```

> **Tại sao dùng Authorization Code thay vì gửi password?**
> - User KHÔNG bao giờ nhập password Google trên app của bạn
> - App KHÔNG biết password Google
> - User có thể revoke quyền bất kỳ lúc nào

> **🔒 Senior note — `state` parameter (bạn không thấy trong sơ đồ trên vì Spring giấu nó đi):**
> Thực tế request ở bước 2 còn có `&state=<random-string>`. Đây là cơ chế **chống CSRF cho chính luồng OAuth2** — nếu thiếu, kẻ tấn công có thể tự tạo 1 authorization code của tài khoản GHÉ (attacker) rồi lừa victim mở link redirect đó, khiến victim vô tình đăng nhập vào tài khoản của attacker (session fixation qua OAuth). Spring Security **tự sinh và verify `state`** cho bạn (lưu tạm trong `HttpSessionOAuth2AuthorizationRequestRepository`) — bạn không cần code tay, nhưng phải **hiểu nó tồn tại**, vì nếu app của bạn stateless (JWT, không session) thì bước này sẽ cần cấu hình lại repository (ví dụ dùng cookie-based thay vì session-based) — đây là lỗi rất hay gặp khi ráp OAuth2 Login vào app đã stateless từ JWT (File 3).

---

## 2. OAuth2 Login (Social Login)

### 2.1 Dependency

```xml
<!-- Thêm vào cùng oauth2-resource-server từ File 3 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

### 2.2 application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}      # Lấy từ Google Cloud Console
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid, profile, email

          github:
            client-id: ${GITHUB_CLIENT_ID}      # Lấy từ GitHub Developer Settings
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: user:email, read:user
```

> **Lấy client-id/secret ở đâu?**
> - Google: https://console.cloud.google.com → APIs & Services → Credentials
> - GitHub: https://github.com/settings/developers → OAuth Apps

### 2.3 User Entity — Mở Rộng Từ File 2

```java
// ===== entity/User.java — THÊM OAuth2 fields =====
// Thêm vào User entity đã tạo ở File 2

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;       // NULL nếu OAuth2 user (không có password)

    // ★ THÊM MỚI cho OAuth2
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;  // LOCAL, GOOGLE, GITHUB

    private String providerId;     // ID từ OAuth2 provider (Google sub, GitHub id)

    private boolean active = true;
    private boolean locked = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}

public enum AuthProvider {
    LOCAL,   // Đăng ký bằng username/password
    GOOGLE,  // Login bằng Google
    GITHUB   // Login bằng GitHub
}
```

### 2.4 CustomOAuth2UserService — Xử Lý User Từ Provider

```java
// ===== security/CustomOAuth2UserService.java =====

/**
 * Được gọi SAU KHI Spring đổi code → access_token → gọi userinfo endpoint.
 * Nhận OAuth2User (thông tin user từ Google/GitHub) → tạo/update user trong DB.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        // 1. Gọi parent → lấy user info từ provider
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. Xác định provider nào
        String registrationId = userRequest.getClientRegistration()
            .getRegistrationId();  // "google" hoặc "github"

        // 3. Extract thông tin (mỗi provider trả format khác nhau)
        String email, name, providerId;

        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = oAuth2User.getAttribute("sub");
        } else if ("github".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("login");
            providerId = String.valueOf(
                (Integer) oAuth2User.getAttribute("id"));
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider");
        }

        // 4. Tạo hoặc update user trong DB
        User user = userRepository.findByEmail(email)
            .map(existingUser -> {
                // ⚠️ XEM GHI CHÚ BẢO MẬT NGAY DƯỚI ĐÂY TRƯỚC KHI COPY ĐOẠN NÀY
                existingUser.setUsername(name);
                existingUser.setProvider(
                    AuthProvider.valueOf(registrationId.toUpperCase()));
                existingUser.setProviderId(providerId);
                return userRepository.save(existingUser);
            })
            .orElseGet(() -> {
                // User mới → tạo với role USER
                Role userRole = roleRepository.findByName("USER")
                    .orElseThrow();
                User newUser = User.builder()
                    .email(email)
                    .username(name)
                    .provider(AuthProvider.valueOf(
                        registrationId.toUpperCase()))
                    .providerId(providerId)
                    .active(true)
                    .roles(Set.of(userRole))
                    .build();
                // password = null (OAuth2 user không cần password)
                return userRepository.save(newUser);
            });

        // 5. Wrap thành CustomOAuth2User
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}
```

> **🔒 Senior note — Account takeover qua auto-link bằng email (lỗi bảo mật thật, không phải lý thuyết):**
> Đoạn `findByEmail(email).map(existingUser -> ...)` ở trên **tự động gắn** tài khoản Google/GitHub vào user đã tồn tại chỉ vì trùng email — đây là lỗ hổng kinh điển. Kịch bản tấn công: nạn nhân đã có tài khoản local `victim@gmail.com` (đăng ký bằng password). Nếu provider OAuth2 cho phép tạo tài khoản với email **chưa xác thực** trùng với email đó (một số provider ít nghiêm ngặt, hoặc tấn công qua provider bên thứ 3 khác), kẻ tấn công login OAuth2 → hệ thống tự gắn vào tài khoản victim → chiếm quyền truy cập.
>
> **Cách né:**
> 1. Với Google/GitHub thật thì email luôn đã verify, nhưng **vẫn nên check tường minh**: `Boolean emailVerified = oAuth2User.getAttribute("email_verified");` (Google có claim này, GitHub thì gọi thêm `GET /user/emails` để lấy `verified: true`) — nếu `false`, từ chối auto-link.
> 2. **Không bao giờ** tự động link nếu tài khoản local đó **chưa từng verify email** qua chính hệ thống của bạn.
> 3. An toàn nhất cho app thật: **không auto-link theo email**. Nếu email trùng nhưng `provider` khác `LOCAL`/khác providerId, trả lỗi "email đã được dùng, vui lòng đăng nhập bằng phương thức cũ" và để user tự chọn link thủ công (có xác thực lại password cũ) — đừng để hệ thống tự quyết.
>
> **🔒 Senior note — GitHub có thể trả `email = null`:**
> User GitHub có thể để email ở chế độ private trong Settings → khi đó claim `"email"` trả về `null`, đoạn code trên sẽ crash hoặc tạo user với email null (tuỳ ràng buộc DB). Thực tế phải gọi thêm `GET https://api.github.com/user/emails` (kèm access_token) để lấy email chính (`primary: true`) — Spring không tự làm bước này giúp bạn.

### 2.5 CustomOAuth2User

```java
// ===== security/CustomOAuth2User.java =====

/**
 * Implement cả OAuth2User VÀ UserDetails
 * → dùng được cho cả OAuth2 login và JWT generation.
 */
public class CustomOAuth2User implements OAuth2User, UserDetails {

    private final User user;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // ===== OAuth2User methods =====
    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public String getName() { return user.getUsername(); }

    // ===== UserDetails methods =====
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Dùng lại logic từ CustomUserDetails (File 2)
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission perm : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(perm.getName()));
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getUsername(); }

    // Custom
    public Long getUserId() { return user.getId(); }
    public String getEmail() { return user.getEmail(); }
}
```

### 2.6 OAuth2 Login → Tạo JWT

```java
// ===== security/OAuth2LoginSuccessHandler.java =====

/**
 * Sau khi OAuth2 login thành công → tạo JWT → redirect về frontend.
 * Dùng JwtService từ File 3.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;  // Từ File 3

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOAuth2User oAuth2User =
            (CustomOAuth2User) authentication.getPrincipal();

        // Dùng JwtService tạo token (giống login thường)
        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication);

        // Redirect về frontend kèm tokens
        String targetUrl = UriComponentsBuilder
            .fromUriString("http://localhost:3000/oauth2/callback")
            .queryParam("access_token", accessToken)
            .queryParam("refresh_token", refreshToken)
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
```

> **🔒 Senior note — Đây là pattern KHÔNG NÊN dùng ở production, dù rất phổ biến trong tutorial:**
> Đặt `access_token`/`refresh_token` thật vào query string rồi redirect có 3 vấn đề:
> 1. **Browser history** — token nằm vĩnh viễn trong lịch sử duyệt web của user.
> 2. **Referrer header** — nếu trang `/oauth2/callback` của frontend load thêm 1 tài nguyên bên thứ 3 (ảnh, script, font từ CDN...), trình duyệt gửi URL hiện tại (kèm token) qua header `Referer` tới domain đó.
> 3. **Server access log** — nếu có reverse proxy/CDN (Nginx, Cloudflare) log full URL request, token bị ghi ra log dạng plaintext.
>
> **Pattern thực tế các công ty dùng (chọn 1 trong 2):**
>
> **Cách A — One-time exchange code (khuyên dùng nếu vẫn phải redirect qua URL):**
> ```java
> // Thay vì nhét JWT thật, tạo 1 mã dùng-một-lần, sống 30-60 giây, lưu Redis/cache
> String exchangeCode = UUID.randomUUID().toString();
> redisTemplate.opsForValue().set("oauth_exchange:" + exchangeCode,
>     accessToken + "::" + refreshToken, Duration.ofSeconds(60));
>
> String targetUrl = UriComponentsBuilder
>     .fromUriString("http://localhost:3000/oauth2/callback")
>     .queryParam("code", exchangeCode)   // KHÔNG phải JWT thật
>     .build().toUriString();
> // Frontend nhận "code" → gọi POST /api/auth/exchange {code} → backend trả JWT thật qua response body (không qua URL)
> ```
>
> **Cách B — Refresh token qua httpOnly cookie, chỉ access token (ngắn hạn) mới đi qua URL/body:**
> Set refresh token bằng `Set-Cookie: refresh_token=...; HttpOnly; Secure; SameSite=Strict` ngay trong response redirect — JS phía frontend không đọc được cookie này (chống XSS đánh cắp refresh token), chỉ access token (sống ngắn, thiệt hại giới hạn) mới lộ ra URL.
>
> Việc chọn cách nào phụ thuộc frontend là SPA cùng domain hay khác domain (SameSite/CORS) — nhưng nguyên tắc chung: **refresh token không bao giờ nên chạm vào URL hay localStorage**.

### 2.7 SecurityConfig — Tích Hợp OAuth2 Login

```java
// ===== Thêm vào SecurityConfig (cập nhật từ File 3) =====

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    http
        // ... giữ nguyên cors, csrf, session, authorizeHttpRequests
        // ... giữ nguyên oauth2ResourceServer (File 3)
        // ... giữ nguyên exceptionHandling (File 2)

        // ★ THÊM MỚI — OAuth2 Login
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo ->
                userInfo.userService(customOAuth2UserService)
            )
            .successHandler(oAuth2LoginSuccessHandler)
        );

    return http.build();
}
```

---

## 3. OAuth2 Resource Server — API Nhận JWT Từ Bên Ngoài

> Khi app của bạn **KHÔNG tự tạo JWT** mà nhận JWT từ **Authorization Server bên ngoài**
> (Keycloak, Auth0, Okta).

```java
// application.yml — chỉ cần issuer-uri
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-keycloak.com/realms/your-realm
          # Spring tự động fetch public key từ issuer

// SecurityConfig — giống File 3, chỉ khác không cần JwtConfig
// vì Spring auto-config JwtDecoder từ issuer-uri
```

---

## 4. Khi Nào Dùng Gì?

| Tình huống | Giải pháp | File tham khảo |
|-----------|-----------|---------------|
| REST API + SPA | JWT tự tạo (RSA) | File 3 |
| Cần Google/GitHub login | OAuth2 Client + JWT | File 3 + File 4 |
| Microservices + Auth Server chung | OAuth2 Resource Server | File 4 section 3 |
| Enterprise, SSO | Keycloak/Auth0 | File 4 section 3 |
| App nhỏ, ít user | JWT tự tạo là đủ | File 3 |

---

## 5. 🔒 Senior Review — Cạm Bẫy Thực Tế Khi Lên Production

File note gốc dạy đúng **cơ chế**, nhưng thiếu những thứ chỉ lộ ra khi bạn từng bị pentest/audit hoặc bị khai thác thật. Tổng hợp lại:

| # | Vấn đề | Vì sao quan trọng | Cách xử lý |
|---|--------|-------------------|-----------|
| 1 | `state` param chống CSRF | Không tự hiểu nó tồn tại → khi ráp OAuth2 Login vào app stateless (JWT) sẽ toang vì Spring mặc định lưu `state` trong HTTP session | Xem note ở mục 1.2 — cần `AuthorizationRequestRepository` phù hợp nếu app stateless |
| 2 | Account takeover qua auto-link email | Lỗ hổng OWASP thật (CWE-287), không phải lý thuyết suông | Check `email_verified`, không auto-link nếu local account chưa verify — xem note mục 2.4 |
| 3 | GitHub trả `email = null` | Code crash hoặc tạo user rác nếu user để email private | Gọi thêm `GET /user/emails`, lấy email có `primary: true` |
| 4 | Token thật trong URL redirect | Rò rỉ qua browser history, Referrer header, access log | Dùng one-time exchange code hoặc httpOnly cookie — xem note mục 2.6 |
| 5 | `redirect_uri` không được whitelist chặt | Open Redirect — Google/GitHub *có* validate theo config bạn đăng ký, nhưng nếu bạn tự thêm logic redirect động (`?returnTo=`) thì lại là lỗ hổng riêng của bạn | Redirect URI luôn hardcode/config theo môi trường, không bao giờ nhận từ query param của client |
| 6 | Persist access_token của Google/GitHub vào DB "phòng khi cần" | Tăng surface area bị tấn công vô ích nếu bạn không thật sự gọi lại Google API sau này | Chỉ lưu nếu có nhu cầu thật (vd: đọc Google Calendar sau này) — dùng `OAuth2AuthorizedClientService`, mã hoá at rest |
| 7 | PKCE | OAuth 2.1 (bản nháp kế thừa OAuth2, dần thành chuẩn thực tế 2024-2026) khuyến nghị PKCE cho **mọi** client, kể cả confidential | Spring Security tự bật PKCE cho Authorization Code flow từ 5.2+ nếu registration không có `client-secret` (public client); với confidential client (có secret) thì optional nhưng nên cân nhắc nếu roadmap có thêm mobile/SPA sau này |

---

## ✅ Checklist

- [ ] Hiểu OAuth2 roles (Resource Owner, Client, Auth Server, Resource Server)
- [ ] Hiểu Authorization Code Flow (7 bước) + vai trò của `state` (chống CSRF)
- [ ] Config Google/GitHub trong application.yml
- [ ] CustomOAuth2UserService: extract info → tạo/update user, **có check `email_verified` trước khi auto-link**
- [ ] Xử lý GitHub `email = null` (gọi `/user/emails`)
- [ ] OAuth2LoginSuccessHandler: tạo JWT → redirect frontend **qua one-time code hoặc httpOnly cookie, không nhét JWT thật vào URL**
- [ ] Phân biệt OAuth2 Client vs OAuth2 Resource Server
- [ ] Hiểu vì sao KHÔNG nên persist access_token của provider nếu không dùng lại

---

> **Tiếp theo**: Đọc `Phase4.5_BestPractice_Password_CORS_CSRF.md` →
