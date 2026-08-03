import { apiFetch } from "./client";

export function fetchProducts() {
  return apiFetch("/api/products");
}

export function createProduct(data) {
  return apiFetch("/api/products", { method: "POST", body: JSON.stringify(data) });
}

export function updateProduct(id, data) {
  return apiFetch(`/api/products/${id}`, { method: "PUT", body: JSON.stringify(data) });
}

export function deleteProduct(id) {
  return apiFetch(`/api/products/${id}`, { method: "DELETE" });
}
