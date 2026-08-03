import { apiFetch } from "./client";

// Danh sách toàn bộ role trong hệ thống — dùng để hiển thị checkbox chọn role
export function fetchRoles() {
  return apiFetch("/api/roles");
}
