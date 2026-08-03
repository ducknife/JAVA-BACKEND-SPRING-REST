import { useCallback, useEffect, useState } from "react";
import { fetchCategories } from "../api/categories";
import { createProduct, deleteProduct, fetchProducts, updateProduct } from "../api/products";
import ProductForm from "../components/ProductForm";

export default function AdminProductsPage() {
  const [products, setProducts] = useState(null);
  const [categories, setCategories] = useState([]);
  const [editing, setEditing] = useState(null);
  const [error, setError] = useState("");

  const reload = useCallback(async () => {
    try {
      const [p, c] = await Promise.all([fetchProducts(), fetchCategories()]);
      setProducts(p);
      setCategories(c);
    } catch (err) {
      setError(err.message);
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  async function handleSave(data) {
    if (editing?.id) await updateProduct(editing.id, data);
    else await createProduct(data);
    setEditing(null);
    reload();
  }

  async function handleDelete(id) {
    if (!window.confirm("Xoá sản phẩm này?")) return;
    try {
      await deleteProduct(id);
      reload();
    } catch (err) {
      setError(err.message);
    }
  }

  if (error) return <p className="form-error">{error}</p>;
  if (!products) return <div className="page-loading">Đang tải...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Quản lý sản phẩm</h1>
        <button className="btn-primary" onClick={() => setEditing({})}>
          + Thêm sản phẩm
        </button>
      </div>

      {editing && (
        <ProductForm
          categories={categories}
          initialData={editing}
          onSubmit={handleSave}
          onCancel={() => setEditing(null)}
        />
      )}

      <table className="table">
        <thead>
          <tr>
            <th>Tên sản phẩm</th>
            <th>Giá</th>
            <th>Danh mục</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>{p.name}</td>
              <td>{Number(p.price).toLocaleString("vi-VN")} đ</td>
              <td>{p.category?.name || "—"}</td>
              <td className="table-actions">
                <button className="btn-link" onClick={() => setEditing(p)}>
                  Sửa
                </button>
                <button className="btn-link" onClick={() => handleDelete(p.id)}>
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
