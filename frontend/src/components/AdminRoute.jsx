import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

const ADMIN_ROLES = ["ROLE_ADMIN", "ROLE_COLLABORATOR"];

// Giống ProtectedRoute, nhưng thêm điều kiện: phải có role ADMIN/COLLABORATOR
// mới vào được — khớp với @PreAuthorize("hasAnyRole('ADMIN','COLLABORATOR')")
// đang chặn ở backend cho GET /api/users.
export default function AdminRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) return <div className="page-loading">Đang tải...</div>;
  if (!user) return <Navigate to="/login" replace />;

  const canManage = user.roles?.some((r) => ADMIN_ROLES.includes(r));
  if (!canManage) return <Navigate to="/dashboard" replace />;

  return children;
}
