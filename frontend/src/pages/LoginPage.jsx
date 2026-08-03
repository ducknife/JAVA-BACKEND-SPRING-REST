import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// Trang đăng nhập: form username/password + 2 nút đăng nhập qua Google/GitHub
export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await login(username, password);
      navigate("/dashboard");
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  // Điều hướng CẢ TRANG (không phải fetch) sang backend để bắt đầu luồng OAuth2 —
  // đây phải là window.location, không phải fetch(), vì bước này cần browser
  // tự nhảy sang Google/GitHub để user đăng nhập.
  function loginWithProvider(provider) {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/${provider}`;
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <p className="signature">Ducknife</p>
        <h1>Đăng nhập</h1>

        <form onSubmit={handleSubmit} className="form">
          <label>
            Tên đăng nhập
            <input value={username} onChange={(e) => setUsername(e.target.value)} required />
          </label>
          <label>
            Mật khẩu
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>

          {error && <p className="form-error">{error}</p>}

          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? "Đang đăng nhập..." : "Đăng nhập"}
          </button>
        </form>

        <div className="divider">hoặc</div>

        <div className="oauth-buttons">
          <button className="btn-outline" onClick={() => loginWithProvider("google")}>
            Đăng nhập với Google
          </button>
          <button className="btn-outline" onClick={() => loginWithProvider("github")}>
            Đăng nhập với GitHub
          </button>
        </div>
      </div>
    </div>
  );
}
