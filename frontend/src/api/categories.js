import { apiFetch } from "./client";

export function fetchCategories() {
  return apiFetch("/api/categories");
}

export function createCategory(data) {
  return apiFetch("/api/categories", { method: "POST", body: JSON.stringify(data) });
}

export function updateCategory(id, data) {
  return apiFetch(`/api/categories/${id}`, { method: "PUT", body: JSON.stringify(data) });
}

export function deleteCategory(id) {
  return apiFetch(`/api/categories/${id}`, { method: "DELETE" });
}
