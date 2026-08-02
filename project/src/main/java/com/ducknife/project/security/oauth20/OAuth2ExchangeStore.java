package com.ducknife.project.security.oauth20;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.ducknife.project.common.exception.UnauthorizedException;
import com.ducknife.project.modules.auth.dto.AuthResponse;

@Service
public class OAuth2ExchangeStore {
    // lưu tạm vào map JWT tạo nội bộ cho code tương ứng

    private record Entry(AuthResponse tokens, Instant expiresAt) {}
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public String save(AuthResponse tokens) {
        // sinh 1 code mới để sau này FE đổi code đó để lấy tokens
        String code = UUID.randomUUID().toString();
        store.put(code, new Entry(tokens, Instant.now().plusSeconds(60)));
        return code;
    }

    public AuthResponse consume(String code) {
        Entry entry = store.remove(code); // xóa ngay vì dùng 1 lần
        if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
            throw new UnauthorizedException("Mã exchange không hợp lệ hoặc đã hết hạn!");
        }
        return entry.tokens;
    }
}
