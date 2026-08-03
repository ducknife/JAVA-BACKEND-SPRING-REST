import { useAuth } from "../auth/useAuth";

// Trang chính sau khi đăng nhập — hiển thị thông tin user lấy từ /api/users/me
export default function DashboardPage() {
  const { user } = useAuth();

  return (
    <div className="dashboard">
      <h1>Tổng quan</h1>
      <div className="card">
        <h2>Thông tin tài khoản</h2>
        <dl className="info-list">
          <dt>Họ tên</dt>
          <dd>{user.fullname}</dd>
          <dt>Tên đăng nhập</dt>
          <dd>{user.username}</dd>
          <dt>Email</dt>
          <dd>{user.email || "—"}</dd>
          <dt>Vai trò</dt>
          <dd>{user.roles?.join(", ") || "—"}</dd>
        </dl>
      </div>
    </div>
  );
}
