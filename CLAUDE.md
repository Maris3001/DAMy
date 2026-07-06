# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Dự án

**Đề tài tốt nghiệp:** Xây dựng website bán vé xem phim trực tuyến tích hợp ưu đãi khách hàng thành viên — **"Linh Vé Các"**.

Trạng thái: **P2 hoàn thành** — monorepo scaffold + kết nối DB (P1); auth JWT hoàn chỉnh (P2): đăng ký/đăng nhập/hồ sơ, role USER/ADMIN, admin seed `admin@linhvecac.vn` / `Admin@123456` (dev). Các phase tiếp theo (P3→P8) theo `PLAN.md`. Cập nhật file này khi cấu trúc thực tế thay đổi.

**Chi tiết môi trường đã dựng (P1):**
- Backend: Spring Boot **3.5.16**, Java 21, group `com.linhvecac`. Deps: web, data-jpa, security, validation, flyway-core, flyway-sqlserver, mssql-jdbc, lombok, jjwt 0.12.6.
- Frontend: Vite 8 + Vue 3.5, Pinia 3, Vue Router 5, axios, Tailwind CSS v4 (`@tailwindcss/vite`). Design tokens trong `frontend/src/assets/main.css` (`@theme`); font Be Vietnam Pro self-host trong `frontend/src/assets/fonts/`.
- DB: SQL Server SQLEXPRESS, DB `linhvecac`. **JDBC nối thẳng `localhost:1433`** (không dùng `instanceName` vì SQL Browser tắt). Dev dùng login `sa` trong `application-local.properties` (gitignored); script tạo DB + login riêng ở `backend/db/00_bootstrap.sql`.
- Kết nối local KHÔNG commit: `backend/src/main/resources/application-local.properties` (đã gitignore) — chứa `DB_URL/DB_USER/DB_PASSWORD` và `JWT_SECRET` (Base64 ≥ 32 bytes, map vào `app.jwt.secret`).

**Auth (P2):** JWT HS256 stateless — `JwtAuthenticationFilter` nạp `User` từ DB làm principal (`@AuthenticationPrincipal User`), authority `ROLE_USER|ROLE_ADMIN`; `/api/auth/**` + `/api/health` permitAll, `/api/admin/**` cần ADMIN, còn lại authenticated. Lỗi trả JSON tiếng Việt thống nhất qua `GlobalExceptionHandler`/`ErrorResponse` (`{message, errors}`); lỗi nghiệp vụ ném `ApiException(HttpStatus, message)`. FE: token localStorage `lvc_token`, interceptor trong `src/api/http.js` (gắn Bearer, auto-logout 401), guard `requiresAuth`/`guestOnly` trong router; component chuẩn tại `src/components/ui/`.

Tài liệu đi kèm:
- `PLAN.md` — kế hoạch triển khai 8 phase (có checkbox tiến độ), schema DB, thiết kế VNPay/loyalty. Làm theo thứ tự phase trong file này.
- `DESIGN.md` — quy ước thiết kế UI (màu, chữ, component chuẩn, trạng thái ghế/hạng) và bộ prompt sinh giao diện. Mọi UI mới phải tuân theo file này.

## Tech stack

| Thành phần | Công nghệ |
|---|---|
| Backend | Java Spring Boot (REST API) |
| Frontend | Vue 3 + Vite |
| Database | Microsoft SQL Server |

Cấu trúc dự kiến: monorepo với hai thư mục `backend/` (Spring Boot) và `frontend/` (Vite + Vue).

## Lệnh thường dùng

### Backend (`backend/`)
```bash
./mvnw spring-boot:run          # chạy dev server (mặc định port 8080)
./mvnw test                     # chạy toàn bộ test
./mvnw test -Dtest=TenTestClass # chạy một test class
./mvnw test -Dtest=TenTestClass#tenMethod  # chạy một test method
./mvnw clean package            # build jar
```
Trên Windows PowerShell dùng `.\mvnw.cmd` thay cho `./mvnw`.

### Frontend (`frontend/`)
```bash
npm install        # cài dependencies
npm run dev        # dev server Vite (mặc định port 5173)
npm run build      # build production
```

Dev: frontend gọi API backend qua proxy cấu hình trong `vite.config` (proxy `/api` → `http://localhost:8080`).

## Luồng nghiệp vụ cốt lõi

### 1. Đặt vé (booking flow — theo đúng thứ tự các bước)
1. Chọn **khu vực** (tỉnh/thành)
2. Chọn **hãng rạp & rạp** (cinema chain → cinema)
3. Chọn **ngày & suất chiếu** (showtime)
4. Chọn **ghế** (seat) — cần xử lý giữ ghế tạm thời để tránh 2 người đặt trùng ghế
5. Chọn **đồ ăn, nước** (concessions/combo)
6. **Xác nhận & thanh toán** — áp dụng voucher/ưu đãi tự động tại bước này

### 2. Hệ thống thành viên & ưu đãi (điểm khác biệt của đề tài)
- Mỗi giao dịch thành công → **tích điểm** cho thành viên
- Điểm tích lũy quyết định **hạng thành viên**: Silver / Gold / Platinum
- Hệ thống sinh **ưu đãi phù hợp** dựa trên: hạng thành viên + lịch sử mua vé
- Khi đặt vé tiếp theo: **tự động áp dụng** voucher/discount tốt nhất
- Người dùng theo dõi được **lịch sử tiêu dùng** (giao dịch, điểm, voucher đã dùng)

### Phạm vi chức năng
Yêu cầu mức trung bình: đặt vé online, CRUD phim/lịch chiếu (admin), đăng ký thành viên, tích điểm cơ bản.

Yêu cầu mức khá: phân hạng thành viên, voucher đổi từ điểm tích lũy, ưu đãi theo lịch sử mua vé, quản lý chiến dịch khuyến mãi (admin).

## Domain model chính

Các entity trung tâm và quan hệ:

- **Region** (khu vực) → **CinemaChain** (hãng rạp) → **Cinema** (rạp) → **Room** (phòng chiếu) → **Seat** (ghế)
- **Movie** (phim) → **Showtime** (suất chiếu, thuộc Room + Movie)
- **Booking** (đơn đặt vé) gồm: Ticket (ghế × suất chiếu) + ConcessionItem (đồ ăn/nước) + Payment
- **User/Member** — có `points` (điểm), `tier` (Silver/Gold/Platinum), lịch sử giao dịch
- **Voucher / Promotion / Campaign** — chiến dịch khuyến mãi, voucher gắn với hạng/điểm/lịch sử

Quy tắc nghiệp vụ cần nhất quán toàn hệ thống:
- Tính điểm và xét hạng phải nằm ở **một service duy nhất** phía backend (không tính ở frontend)
- Việc áp voucher tự động chọn ưu đãi **có lợi nhất** cho khách tại bước thanh toán
- Ghế đã có người giữ/đặt phải bị khóa ở tầng DB (tránh race condition khi đặt trùng)

## Kiến trúc backend

Theo layered architecture chuẩn Spring Boot:
```
controller → service → repository (Spring Data JPA) → SQL Server
```
- REST API prefix `/api`, trả JSON; DTO tách khỏi entity
- Xác thực: Spring Security + JWT (phân quyền `USER` / `ADMIN`)
- Cấu hình kết nối SQL Server trong `application.properties` (driver `mssql-jdbc`); thông tin kết nối local không commit — dùng biến môi trường hoặc file profile local

## Kiến trúc frontend

- Vue 3 (Composition API) + Vite
- Vue Router: tách khu vực người dùng (đặt vé, tài khoản, lịch sử) và khu vực admin (CRUD phim/lịch chiếu, quản lý khuyến mãi)
- State cho luồng đặt vé nhiều bước (khu vực → rạp → suất → ghế → đồ ăn → thanh toán) dùng Pinia store, giữ được trạng thái khi chuyển bước
- Gọi API qua một client tập trung (axios/fetch wrapper) gắn JWT

## Quy ước

- Ngôn ngữ giao tiếp với người dùng dự án: **tiếng Việt**; UI hiển thị tiếng Việt
- Tên code (class, biến, API path, bảng DB): **tiếng Anh**
- Tiền tệ: VND (số nguyên, không dùng số thập phân cho giá tiền)
