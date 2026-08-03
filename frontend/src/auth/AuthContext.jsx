import { createContext, useCallback, useEffect, useState } from "react";
import { apiFetch } from "../api/client";

// Nơi lưu trạng thái đăng nhập (user hiện tại) dùng chung cho toàn app
export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Đọc access token đang có -> gọi /api/users/me để lấy thông tin user
  const loadCurrentUser = useCallback(async () => {
    if (!localStorage.getItem("accessToken")) {
      setUser(null);
      setLoading(false);
      return;
    }
    try {
      setUser(await apiFetch("/api/users/me"));
    } catch {
      // Token hỏng/hết hạn -> coi như chưa đăng nhập, dọn sạch token cũ
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  // Lần đầu load trang: thử khôi phục phiên đăng nhập từ token đã lưu
  useEffect(() => {
    loadCurrentUser();
  }, [loadCurrentUser]);

  // Lưu cặp token mới nhận rồi load lại thông tin user
  const saveTokensAndLoadUser = useCallback(
    async (tokens) => {
      localStorage.setItem("accessToken", tokens.accessToken);
      localStorage.setItem("refreshToken", tokens.refreshToken);
      await loadCurrentUser();
    },
    [loadCurrentUser]
  );

  // Đăng nhập username/password thường
  const login = useCallback(
    async (username, password) => {
      const tokens = await apiFetch("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });
      await saveTokensAndLoadUser(tokens);
    },
    [saveTokensAndLoadUser]
  );

  // Đổi exchange code (từ OAuth2 redirect) lấy JWT thật
  const exchangeOAuth2Code = useCallback(
    async (code) => {
      const tokens = await apiFetch(`/api/auth/oauth2/exchange?code=${encodeURIComponent(code)}`);
      await saveTokensAndLoadUser(tokens);
    },
    [saveTokensAndLoadUser]
  );

  // Đăng xuất: thu hồi cả access lẫn refresh token ở backend rồi xoá token local
  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem("refreshToken");
    try {
      await apiFetch("/api/auth/logout", {
        method: "POST",
        body: JSON.stringify({ refreshToken }),
      });
    } finally {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      setUser(null);
    }
  }, []);

  const value = { user, loading, login, logout, exchangeOAuth2Code };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
