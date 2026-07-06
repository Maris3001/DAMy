-- V1 baseline: bảng metadata tối thiểu để kiểm chứng Flyway + endpoint /api/health.
-- Quy ước SQL Server: NVARCHAR + literal N'...' cho tiếng Việt; KHÔNG dùng GO/USE trong migration.
CREATE TABLE app_meta (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    meta_key   NVARCHAR(100)  NOT NULL UNIQUE,
    meta_value NVARCHAR(400)  NOT NULL
);

INSERT INTO app_meta (meta_key, meta_value) VALUES
    (N'app_name', N'Linh Vé Các'),
    (N'schema_version', N'P1');
