import { useContext } from "react";
import { AuthContext } from "./AuthContext";

// Hook tiện dùng để đọc trạng thái đăng nhập ở bất kỳ component nào
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth phải được gọi bên trong <AuthProvider>");
  return ctx;
}
