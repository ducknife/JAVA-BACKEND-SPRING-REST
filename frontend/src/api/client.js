const BASE_URL = import.meta.env.VITE_API_BASE_URL;

/**
 * Gọi API backend, tự gắn Bearer token (nếu có) và bóc tách field "data"
 * khỏi envelope chuẩn { status, message, data } mà backend luôn trả về.
 */
export async function apiFetch(path, options = {}) {
  const token = localStorage.getItem("accessToken");

  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  const body = await res.json().catch(() => null);

  if (!res.ok) {
    throw new Error(body?.message || `Lỗi ${res.status}`);
  }
  return body?.data;
}
