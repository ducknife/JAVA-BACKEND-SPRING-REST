import { apiFetch } from "./client";

// Danh sách user có phân trang — backend yêu cầu role ADMIN hoặc COLLABORATOR
export function fetchUsers(page = 0, size = 10) {
  return apiFetch(`/api/users?page=${page}&size=${size}&sort=fullname,asc`);
}

export function createUser(data) {
  return apiFetch("/api/users", { method: "POST", body: JSON.stringify(data) });
}

// Lưu ý: backend bắt buộc phải gửi kèm "password" kể cả khi chỉ sửa thông tin khác
export function updateUser(id, data) {
  return apiFetch(`/api/users/${id}`, { method: "PUT", body: JSON.stringify(data) });
}

// Chỉ role ADMIN mới xoá được (COLLABORATOR không đủ quyền)
export function deleteUser(id) {
  return apiFetch(`/api/users/${id}`, { method: "DELETE" });
}
