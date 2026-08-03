import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createOrder } from "../api/orders";
import { fetchProducts } from "../api/products";
import { useAuth } from "../auth/useAuth";

// Trang cho user thường: xem danh sách sản phẩm, nhập số lượng muốn mua,
// gộp thành 1 đơn hàng rồi gửi lên POST /api/orders.
export default function OrdersPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [products, setProducts] = useState(null);
  const [quantities, setQuantities] = useState({}); // { productId: quantity }
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchProducts()
      .then(setProducts)
      .catch((err) => setError(err.message));
  }, []);

  function setQuantity(productId, value) {
    setQuantities((prev) => ({ ...prev, [productId]: Math.max(0, Number(value) || 0) }));
  }

  const cartItems = Object.entries(quantities).filter(([, qty]) => qty > 0);
  const total = cartItems.reduce((sum, [productId, qty]) => {
    const product = products?.find((p) => String(p.id) === productId);
    return sum + (product ? Number(product.price) * qty : 0);
  }, 0);

  async function handleOrder() {
    setError("");
    setSubmitting(true);
    try {
      await createOrder({
        userId: user.userId,
        orderDetails: cartItems.map(([productId, qty]) => ({
          productId: Number(productId),
          quantity: qty,
        })),
      });
      navigate("/my-orders");
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  if (error && !products) return <p className="form-error">{error}</p>;
  if (!products) return <div className="page-loading">Đang tải...</div>;

  return (
    <div>
      <h1>Sản phẩm</h1>

      <table className="table">
        <thead>
          <tr>
            <th>Tên sản phẩm</th>
            <th>Giá</th>
            <th>Danh mục</th>
            <th>Số lượng</th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>{p.name}</td>
              <td>{Number(p.price).toLocaleString("vi-VN")} đ</td>
              <td>{p.category?.name || "—"}</td>
              <td>
                <input
                  type="number"
                  min="0"
                  className="qty-input"
                  value={quantities[p.id] || ""}
                  onChange={(e) => setQuantity(p.id, e.target.value)}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="card order-summary">
        <h2>Đơn hàng của bạn</h2>
        {cartItems.length === 0 ? (
          <p>Chưa chọn sản phẩm nào.</p>
        ) : (
          <p>
            Tổng cộng: <strong>{total.toLocaleString("vi-VN")} đ</strong>
          </p>
        )}
        {error && <p className="form-error">{error}</p>}
        <button
          className="btn-primary"
          disabled={cartItems.length === 0 || submitting}
          onClick={handleOrder}
        >
          {submitting ? "Đang đặt hàng..." : "Đặt hàng"}
        </button>
      </div>
    </div>
  );
}
