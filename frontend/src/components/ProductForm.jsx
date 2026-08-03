import { useState } from "react";

// Form dùng chung tạo/sửa sản phẩm — cần chọn category (backend đòi category_id)
export default function ProductForm({ categories, initialData, onSubmit, onCancel }) {
  const isEdit = Boolean(initialData?.id);
  const [name, setName] = useState(initialData?.name || "");
  const [price, setPrice] = useState(initialData?.price ?? "");
  const [categoryId, setCategoryId] = useState(initialData?.category?.id ?? "");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await onSubmit({ name, price: Number(price), category_id: Number(categoryId) });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="form card user-form">
      <h2>{isEdit ? "Sửa sản phẩm" : "Thêm sản phẩm"}</h2>

      <label>
        Tên sản phẩm
        <input value={name} onChange={(e) => setName(e.target.value)} required />
      </label>

      <label>
        Giá (VNĐ)
        <input
          type="number"
          min="0"
          step="1000"
          value={price}
          onChange={(e) => setPrice(e.target.value)}
          required
        />
      </label>

      <label>
        Danh mục
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
          <option value="" disabled>
            -- Chọn danh mục --
          </option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </label>

      {error && <p className="form-error">{error}</p>}

      <div className="form-actions">
        <button type="submit" className="btn-primary" disabled={submitting}>
          {submitting ? "Đang lưu..." : "Lưu"}
        </button>
        <button type="button" className="btn-outline" onClick={onCancel}>
          Huỷ
        </button>
      </div>
    </form>
  );
}
