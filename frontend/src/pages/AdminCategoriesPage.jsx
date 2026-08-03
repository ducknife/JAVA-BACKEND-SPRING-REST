import { useCallback, useEffect, useState } from "react";
import { createCategory, deleteCategory, fetchCategories, updateCategory } from "../api/categories";
import CategoryForm from "../components/CategoryForm";

export default function AdminCategoriesPage() {
  const [categories, setCategories] = useState(null);
  const [editing, setEditing] = useState(null);
  const [error, setError] = useState("");

  const reload = useCallback(async () => {
    try {
      setCategories(await fetchCategories());
    } catch (err) {
      setError(err.message);
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  async function handleSave(data) {
    if (editing?.id) await updateCategory(editing.id, data);
    else await createCategory(data);
    setEditing(null);
    reload();
  }

  async function handleDelete(id) {
    if (!window.confirm("Xoá danh mục này?")) return;
    try {
      await deleteCategory(id);
      reload();
    } catch (err) {
      setError(err.message);
    }
  }

  if (error) return <p className="form-error">{error}</p>;
  if (!categories) return <div className="page-loading">Đang tải...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Quản lý danh mục</h1>
        <button className="btn-primary" onClick={() => setEditing({})}>
          + Thêm danh mục
        </button>
      </div>

      {editing && (
        <CategoryForm initialData={editing} onSubmit={handleSave} onCancel={() => setEditing(null)} />
      )}

      <table className="table">
        <thead>
          <tr>
            <th>Tên danh mục</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {categories.map((c) => (
            <tr key={c.id}>
              <td>{c.name}</td>
              <td className="table-actions">
                <button className="btn-link" onClick={() => setEditing(c)}>
                  Sửa
                </button>
                <button className="btn-link" onClick={() => handleDelete(c.id)}>
                  Xoá
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
