import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

// Bọc quanh route cần đăng nhập — chưa có user thì đá về /login
export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) return <div className="page-loading">Đang tải...</div>;
  if (!user) return <Navigate to="/login" replace />;

  return children;
}
