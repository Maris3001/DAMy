-- ==========================================================================
-- Bootstrap DB "linhvecac" + SQL login riêng cho ứng dụng.
-- Chạy MỘT LẦN bằng tài khoản admin/sa (server-level CREATE LOGIN):
--   sqlcmd -S localhost\SQLEXPRESS -E -i backend\db\00_bootstrap.sql
--   (hoặc -U sa -P <sa_password> nếu không có quyền Windows admin)
--
-- Mật khẩu dưới đây phải KHỚP DB_PASSWORD trong application-local.properties.
-- File này dùng GO vì chạy qua sqlcmd — KHÁC migration Flyway (không được dùng GO).
-- ==========================================================================

IF DB_ID('linhvecac') IS NULL
    CREATE DATABASE linhvecac;
GO

IF SUSER_ID('linhvecac_app') IS NULL
    CREATE LOGIN linhvecac_app
        WITH PASSWORD = 'LinhVeCac@2026',
             CHECK_POLICY = OFF,
             DEFAULT_DATABASE = linhvecac;
GO

USE linhvecac;
GO

IF USER_ID('linhvecac_app') IS NULL
    CREATE USER linhvecac_app FOR LOGIN linhvecac_app;
GO

-- Dev: cấp db_owner để Flyway tạo/sửa schema thoải mái.
ALTER ROLE db_owner ADD MEMBER linhvecac_app;
GO

PRINT 'Bootstrap hoàn tất: DB linhvecac + login linhvecac_app (db_owner).';
GO
