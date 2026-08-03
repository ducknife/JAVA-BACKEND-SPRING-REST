import { useState } from "react";

// Form dùng chung tạo/sửa danh mục — chỉ có 1 field duy nhất
export default function CategoryForm({ initialData, onSubmit, onCancel }) {
  const isEdit = Boolean(initialData?.id);
  const [name, setName] = useState(initialData?.name || "");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await onSubmit({ name });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="form card user-form">
      <h2>{isEdit ? "Sửa danh mục" : "Thêm danh mục"}</h2>
      <label>
        Tên danh mục
        <input value={name} onChange={(e) => setName(e.target.value)} required />
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
