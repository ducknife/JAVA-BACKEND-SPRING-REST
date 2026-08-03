import { apiFetch } from "./client";

// Đơn hàng của 1 user cụ thể (dùng cho "Đơn hàng của tôi")
export function fetchMyOrders(userId) {
  return apiFetch(`/api/users/${userId}/orders`);
}

export function createOrder(data) {
  return apiFetch("/api/orders", { method: "POST", body: JSON.stringify(data) });
}
