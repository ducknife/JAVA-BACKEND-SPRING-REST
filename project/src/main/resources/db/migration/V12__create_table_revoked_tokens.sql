create table revoked_tokens (
    jti varchar(36) primary key not null, /* bảng này lưu token bị thu hồi */
    expires_at timestamp not null
);