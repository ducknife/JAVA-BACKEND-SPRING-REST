import { useEffect, useRef, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

// Trang trung gian sau khi backend redirect về từ Google/GitHub.
// Đọc "code" (exchange code tự chế, không phải code của Google/GitHub) từ URL,
// đổi lấy JWT thật qua /api/auth/oauth2/exchange.
export default function OAuth2CallbackPage() {
  const [searchParams] = useSearchParams();
  const { exchangeOAuth2Code } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState("");

  // React.StrictMode (chỉ ở dev) cố tình chạy useEffect 2 lần để bắt lỗi side-effect.
  // "code" chỉ dùng được 1 lần (backend xoá ngay sau khi đọc) nên phải tự chặn
  // lần gọi thứ 2, nếu không sẽ bị lỗi "code không hợp lệ" dù lần đầu đã thành công.
  const hasExchanged = useRef(false);

  useEffect(() => {
    if (hasExchanged.current) return;
    hasExchanged.current = true;

    const code = searchParams.get("code");
    if (!code) {
      setError("Thiếu mã xác thực trên URL.");
      return;
    }
    exchangeOAuth2Code(code)
      .then(() => navigate("/dashboard"))
      .catch((err) => setError(err.message));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (error) {
    return (
      <div className="auth-page">
        <div className="auth-card">
          <h1>Đăng nhập thất bại</h1>
          <p className="form-error">{error}</p>
          <Link to="/login" className="btn-primary">
            Quay lại đăng nhập
          </Link>
        </div>
      </div>
    );
  }

  return <div className="page-loading">Đang xử lý đăng nhập...</div>;
}
