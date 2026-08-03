import { useState } from "react";

/**
 * Form dùng chung cho cả "tạo mới" và "sửa" user.
 * initialData rỗng ({}) => chế độ tạo mới; có userId => chế độ sửa.
 *
 * Lưu ý quan trọng: backend (PUT /api/users/{id}) bắt buộc phải có "password"
 * trong request kể cả khi chỉ sửa họ tên/role — nên form luôn bắt nhập password.
 */
export default function UserForm({ roles, initialData, onSubmit, onCancel }) {
  const isEdit = Boolean(initialData?.userId);

  const [fullname, setFullname] = useState(initialData?.fullname || "");
  const [username, setUsername] = useState(initialData?.username || "");
  const [password, setPassword] = useState("");
  const [selectedRoles, setSelectedRoles] = useState(new Set(initialData?.roles || []));
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function toggleRole(name) {
    const next = new Set(selectedRoles);
    if (next.has(name)) next.delete(name);
    else next.add(name);
    setSelectedRoles(next);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await onSubmit({ fullname, username, password, roles: Array.from(selectedRoles) });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="form card user-form">
      <h2>{isEdit ? "Sửa người dùng" : "Thêm người dùng"}</h2>

      <label>
        Họ tên
        <input value={fullname} onChange={(e) => setFullname(e.target.value)} required />
      </label>

      <label>
        Tài khoản
        <input value={username} onChange={(e) => setUsername(e.target.value)} required />
      </label>

      <label>
        Mật khẩu {isEdit && "(bắt buộc nhập lại — backend yêu cầu)"}
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
      </label>

      <fieldset className="role-fieldset">
        <legend>Vai trò</legend>
        {roles.map((r) => (
          <label key={r.id} className="role-checkbox">
            <input type="checkbox" checked={selectedRoles.has(r.name)} onChange={() => toggleRole(r.name)} />
            {r.name}
          </label>
        ))}
      </fieldset>

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
