---
name: verify
description: Cách build, chạy và lái ứng dụng Linh Vé Các để xác minh thay đổi end-to-end (backend Spring Boot + frontend Vue qua Playwright).
---

# Verify — Linh Vé Các

## Khởi động

1. **Backend** (cần SQL Server SQLEXPRESS đang chạy, port tĩnh 1433):
   ```powershell
   cd backend; .\mvnw.cmd spring-boot:run   # chạy nền, port 8080
   ```
   Chờ log `Started LinhvecacApplication`. Flyway tự apply migration khi boot.
   Smoke check: `GET http://localhost:8080/api/health` → `{status:UP, db:UP}`.

2. **Frontend**:
   ```powershell
   cd frontend; npm run dev                 # port 5173, proxy /api → 8080
   ```

## Lái surface

- **API**: PowerShell `Invoke-RestMethod -SkipHttpErrorCheck -StatusCodeVariable sc` để bắt cả status lỗi. Auth: lấy token từ `POST /api/auth/login` rồi gắn header `Authorization: Bearer`.
- **UI**: Playwright (cài vào scratchpad, KHÔNG cài vào project):
  ```bash
  cd <scratchpad> && npm init -y && npm i playwright && npx playwright install chromium
  ```
  Viết script `.mjs` lái `http://localhost:5173`, screenshot vào scratchpad.

## Dữ liệu có sẵn

- Admin seed: `admin@linhvecac.vn` / `Admin@123456` (V3).
- Tạo user test bằng email unique theo timestamp (`e2e<ts>@test.vn`) — DB dev không reset.

## Gotcha

- Nút trong header trùng tên với nút submit form ("Đăng nhập"/"Đăng ký") → scope selector bằng `page.getByRole('main')`.
- Token lưu localStorage key `lvc_token`; route guard đẩy về `/dang-nhap?redirect=...`.
- Console 401/400/409 khi cố tình gây lỗi là hành vi mong đợi, không phải bug.
