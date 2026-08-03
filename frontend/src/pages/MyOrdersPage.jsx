import { useEffect, useState } from "react";
import { fetchMyOrders } from "../api/orders";
import { useAuth } from "../auth/useAuth";

// Danh sách đơn hàng của chính user đang đăng nhập.
// Backend chưa có khái niệm "trạng thái đơn hàng" (pending/confirmed...) —
// đơn hàng tạo xong là coi như tồn tại luôn, không có bước xác nhận riêng.
export default function MyOrdersPage() {
  const { user } = useAuth();
  const [orders, setOrders] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchMyOrders(user.userId)
      .then(setOrders)
      .catch((err) => setError(err.message));
  }, [user.userId]);

  if (error) return <p className="form-error">{error}</p>;
  if (!orders) return <div className="page-loading">Đang tải...</div>;

  return (
    <div>
      <h1>Đơn hàng của tôi</h1>

      {orders.length === 0 && <p>Bạn chưa có đơn hàng nào.</p>}

      {orders.map((order) => {
        const total = order.orderDetails.reduce((sum, d) => sum + Number(d.price) * d.quantity, 0);
        return (
          <div key={order.id} className="card order-card">
            <h2>Đơn hàng #{order.id}</h2>
            <ul className="order-detail-list">
              {order.orderDetails.map((d) => (
                <li key={d.id}>
                  {d.product.name} × {d.quantity} — {(Number(d.price) * d.quantity).toLocaleString("vi-VN")} đ
                </li>
              ))}
            </ul>
            <p className="order-total">
              Tổng: <strong>{total.toLocaleString("vi-VN")} đ</strong>
            </p>
          </div>
        );
      })}
    </div>
  );
}
