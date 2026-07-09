# Linh Vé Các — Website bán vé xem phim trực tuyến tích hợp ưu đãi thành viên

Đồ án tốt nghiệp: đặt vé xem phim online + hệ thống thành viên (tích điểm, phân hạng, voucher cá nhân hóa). Monorepo gồm `backend/` (Spring Boot REST API) và `frontend/` (Vue 3 + Vite).

## Tính năng chính

- **Đặt vé 6 bước**: khu vực → rạp → suất chiếu → ghế → bắp nước → thanh toán, có **giữ ghế tạm** chống đặt trùng ở tầng DB.
- **Thanh toán** VNPay sandbox (hoặc cổng mock để demo), sinh vé + mã QR.
- **Thành viên**: tích điểm mỗi giao dịch, phân hạng Silver/Gold/Platinum, đổi điểm lấy voucher.
- **Ưu đãi cá nhân hóa** (OfferEngine): sinh nhật, win-back, thể loại yêu thích, quà lên hạng; tự động áp voucher tốt nhất khi thanh toán.
- **Quản trị**: CRUD phim/lịch chiếu/rạp/bắp nước, chiến dịch khuyến mãi, **dashboard doanh thu/top phim/phân bố hạng**.

## Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Security + JWT, Spring Data JPA, Flyway |
| Frontend | Vue 3 (Composition API), Vite, Pinia, Vue Router, Tailwind CSS v4, axios |
| Database | Microsoft SQL Server (SQLEXPRESS) |

## Yêu cầu môi trường

- **Java 21** (LTS)
- **Node.js** 20+ và npm
- **SQL Server** (Express trở lên), bật TCP/IP + đăng nhập SQL (Mixed Mode)

## Chuẩn bị Database

1. Tạo database + login qua script `backend/db/00_bootstrap.sql` (chạy bằng SSMS hoặc `sqlcmd`). Database mặc định: `linhvecac`.
2. Flyway tự động chạy migration (`V1`→`V11`) khi backend khởi động lần đầu — không cần tạo bảng tay.

> Lưu ý Windows/SQLEXPRESS: JDBC nối thẳng `localhost:1433` (không dùng `instanceName` khi SQL Browser tắt). mssql-jdbc yêu cầu `encrypt=true;trustServerCertificate=true`.

## Cấu hình kết nối local (không commit)

Tạo file `backend/src/main/resources/application-local.properties` (đã gitignore) với các biến:

```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=linhvecac;encrypt=true;trustServerCertificate=true
DB_USER=sa
DB_PASSWORD=<mật khẩu SQL của bạn>

# Base64, tối thiểu 32 bytes (HS256)
JWT_SECRET=<chuỗi base64 bí mật>

# (Tùy chọn) VNPay sandbox — để trống sẽ dùng cổng mock
# PAYMENT_PROVIDER=vnpay
# VNPAY_TMN_CODE=...
# VNPAY_HASH_SECRET=...
```

## Chạy Backend (`backend/`)

```bash
# Windows PowerShell
.\mvnw.cmd clean compile      # biên dịch lại (đảm bảo cờ -parameters, xem lưu ý bên dưới)
.\mvnw.cmd spring-boot:run    # chạy dev server, mặc định http://localhost:8080

# chạy test
.\mvnw.cmd test
```

> **Lưu ý**: nếu dùng VS Code với Java extension, extension có thể biên dịch đè `target/classes` làm mất cờ `-parameters` → lỗi 500 khi resolve `@PathVariable`. Chạy `.\mvnw.cmd clean compile` trước `spring-boot:run` để tránh.
>
> Đổi port khi 8080 bị chiếm: đặt biến môi trường `SERVER_PORT`.

## Chạy Frontend (`frontend/`)

```bash
npm install
npm run dev      # http://localhost:5173 (proxy /api → http://localhost:8080)
npm run build    # build production vào dist/
```

## Tài khoản demo

| Vai trò | Email | Mật khẩu |
|---|---|---|
| Admin | `admin@linhvecac.vn` | `Admin@123456` |
| Thành viên | tự đăng ký tại `/dang-ky` | — |

## Luồng demo end-to-end

Đăng ký → đặt vé 6 bước → thanh toán (mock/VNPay sandbox) → nhận vé + điểm → lên hạng → nhận ưu đãi → đặt vé lần 2 thấy voucher tự áp dụng → xem lịch sử (vé/điểm/voucher có filter). Admin xem dashboard doanh thu tại `/admin`.

## Tài liệu

- `PLAN.md` — kế hoạch 8 phase, schema DB, thiết kế VNPay/loyalty.
- `DESIGN.md` — quy ước thiết kế UI (màu, chữ, component chuẩn).
- `CLAUDE.md` — ghi chú kiến trúc chi tiết theo từng phase.
