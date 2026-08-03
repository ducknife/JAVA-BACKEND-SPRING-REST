import { useCallback, useEffect, useState } from "react";
import { createUser, deleteUser, fetchUsers, updateUser } from "../api/users";
import { fetchRoles } from "../api/roles";
import { useAuth } from "../auth/useAuth";
import UserForm from "../components/UserForm";

// Trang quản lý user cho ADMIN/COLLABORATOR: xem danh sách (phân trang),
// tạo mới, sửa, xoá. "editingUser" điều khiển form: null = ẩn, {} = tạo mới,
// object có userId = đang sửa user đó.
export default function AdminUsersPage() {
  const { user } = useAuth();
  const canDelete = user.roles?.includes("ROLE_ADMIN");

  const [page, setPage] = useState(0);
  const [usersPage, setUsersPage] = useState(null);
  const [roles, setRoles] = useState([]);
  const [editingUser, setEditingUser] = useState(null);
  const [error, setError] = useState("");

  const reload = useCallback(async () => {
    try {
      const [users, allRoles] = await Promise.all([fetchUsers(page), fetchRoles()]);
      setUsersPage(users);
      setRoles(allRoles);
    } catch (err) {
      setError(err.message);
    }
  }, [page]);

  useEffect(() => {
    reload();
  }, [reload]);

  async function handleSave(formData) {
    if (editingUser?.userId) {
      await updateUser(editingUser.userId, formData);
    } else {
      await createUser(formData);
    }
    setEditingUser(null);
    reload();
  }

  async function handleDelete(id) {
    if (!window.confirm("Xoá người dùng này? Không thể hoàn tác.")) return;
    try {
      await deleteUser(id);
      reload();
    } catch (err) {
      setError(err.message);
    }
  }

  if (error) return <p className="form-error">{error}</p>;
  if (!usersPage) return <div className="page-loading">Đang tải...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Quản lý người dùng</h1>
        <button className="btn-primary" onClick={() => setEditingUser({})}>
          + Thêm người dùng
        </button>
      </div>

      {editingUser && (
        <UserForm
          roles={roles}
          initialData={editingUser}
          onSubmit={handleSave}
          onCancel={() => setEditingUser(null)}
        />
      )}

      <table className="table">
        <thead>
          <tr>
            <th>Họ tên</th>
            <th>Tài khoản</th>
            <th>Vai trò</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {usersPage.content.map((u) => (
            <tr key={u.userId}>
              <td>{u.fullname}</td>
              <td>{u.username}</td>
              <td>{u.roles.join(", ")}</td>
              <td className="table-actions">
                <button className="btn-link" onClick={() => setEditingUser(u)}>
                  Sửa
                </button>
                {canDelete && (
                  <button className="btn-link" onClick={() => handleDelete(u.userId)}>
                    Xoá
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="pagination">
        <button className="btn-outline" disabled={usersPage.number === 0} onClick={() => setPage((p) => p - 1)}>
          ← Trước
        </button>
        <span>
          Trang {usersPage.number + 1} / {usersPage.totalPages || 1}
        </span>
        <button
          className="btn-outline"
          disabled={usersPage.number + 1 >= usersPage.totalPages}
          onClick={() => setPage((p) => p + 1)}
        >
          Sau →
        </button>
      </div>
    </div>
  );
}
