import { Link } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

const ADMIN_ROLES = ["ROLE_ADMIN", "ROLE_COLLABORATOR"];

// Khung sườn chung cho mọi trang: thanh top bar (logo + menu + user) + nội dung
export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const canManageUsers = user?.roles?.some((r) => ADMIN_ROLES.includes(r));

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar-left">
          <Link to="/" className="logo">
            Ducknife
          </Link>
          {user && (
            <nav className="topbar-nav">
              <Link to="/dashboard">Tổng quan</Link>
              <Link to="/products">Sản phẩm</Link>
              <Link to="/my-orders">Đơn hàng của tôi</Link>
              {canManageUsers && <Link to="/admin/users">Người dùng</Link>}
              {canManageUsers && <Link to="/admin/categories">Danh mục</Link>}
              {canManageUsers && <Link to="/admin/products">QL Sản phẩm</Link>}
            </nav>
          )}
        </div>
        {user && (
          <div className="topbar-user">
            <span>{user.fullname}</span>
            <button className="btn-link" onClick={logout}>
              Đăng xuất
            </button>
          </div>
        )}
      </header>
      <main className="content">{children}</main>
    </div>
  );
}
