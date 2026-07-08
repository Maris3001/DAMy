-- V9: loyalty P6 — tích điểm + phân hạng thành viên.
-- Hai bộ đếm trên users: points_balance (điểm còn dùng — P7 trừ khi đổi voucher) và
-- lifetime_points (xét hạng, không tụt). Ledger point_transactions ghi từng lần cộng/trừ.

-- CHECK khai báo INLINE cùng cột (column-level) — nếu tách thành ALTER ADD CONSTRAINT riêng thì
-- SQL Server phân giải tên trong cùng batch trước khi cột tier tồn tại → "Invalid column name 'tier'".
ALTER TABLE users ADD
    points_balance  BIGINT       NOT NULL CONSTRAINT df_users_points_balance DEFAULT 0,
    lifetime_points BIGINT       NOT NULL CONSTRAINT df_users_lifetime_points DEFAULT 0,
    tier            NVARCHAR(20) NOT NULL CONSTRAINT df_users_tier DEFAULT 'SILVER'
                    CONSTRAINT ck_users_tier CHECK (tier IN ('SILVER', 'GOLD', 'PLATINUM'));

-- Sổ điểm: delta có dấu (+ EARN, − REDEEM), balance_after chốt số dư sau giao dịch để tra soát.
CREATE TABLE point_transactions (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id       BIGINT        NOT NULL,
    booking_id    BIGINT        NULL,
    type          NVARCHAR(20)  NOT NULL,
    delta         BIGINT        NOT NULL,
    balance_after BIGINT        NOT NULL,
    description   NVARCHAR(255) NULL,
    created_at    DATETIME2     NOT NULL CONSTRAINT df_point_txn_created DEFAULT SYSDATETIME(),
    CONSTRAINT fk_point_txn_user    FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_point_txn_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT ck_point_txn_type CHECK (type IN ('EARN', 'REDEEM', 'ADJUST'))
);
CREATE INDEX ix_point_txn_user ON point_transactions(user_id, created_at DESC);

-- Lịch sử đổi hạng: ghi mỗi lần lifetime_points vượt ngưỡng lên hạng mới.
CREATE TABLE tier_history (
    id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    old_tier           NVARCHAR(20) NOT NULL,
    new_tier           NVARCHAR(20) NOT NULL,
    lifetime_points_at BIGINT       NOT NULL,
    created_at         DATETIME2    NOT NULL CONSTRAINT df_tier_history_created DEFAULT SYSDATETIME(),
    CONSTRAINT fk_tier_history_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX ix_tier_history_user ON tier_history(user_id, created_at DESC);
