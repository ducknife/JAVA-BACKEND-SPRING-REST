package com.ducknife.project.security.oauth20;

import com.ducknife.project.modules.user.User;

// Hợp đồng dùng chung cho mọi provider (openid lẫn non-openid) -> tìm hoặc tạo user local
// tương ứng với danh tính bên ngoài. Chỉ có 1 implementation, các provider chỉ khác nhau
// ở chỗ lấy claim (email, name, providerId, emailVerified) như thế nào trước khi gọi vào đây.
public interface OAuth2AccountLinker {
    User findOrCreateUser(String email, String name, String providerId,
                           String providerName, boolean emailVerified);
}
