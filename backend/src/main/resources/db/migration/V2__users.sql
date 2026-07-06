-- V2: bảng users — nền tảng auth P2. Cột điểm/hạng thành viên sẽ ALTER ở V8 (P6 loyalty).
CREATE TABLE users (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    email         NVARCHAR(255) NOT NULL UNIQUE,
    password_hash NVARCHAR(72)  NOT NULL,
    full_name     NVARCHAR(150) NOT NULL,
    phone         NVARCHAR(20)  NULL,
    birth_date    DATE          NULL, -- nhận ưu đãi sinh nhật (P7)
    role          NVARCHAR(20)  NOT NULL CONSTRAINT df_users_role DEFAULT 'USER',
    created_at    DATETIME2     NOT NULL CONSTRAINT df_users_created DEFAULT SYSDATETIME(),
    CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN'))
);
