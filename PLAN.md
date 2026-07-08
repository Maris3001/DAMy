# Kế hoạch tạo dự án "Linh Vé Các"

## Bối cảnh

Đồ án tốt nghiệp: website bán vé xem phim trực tuyến tích hợp ưu đãi khách hàng thành viên. Dự án hoàn toàn mới, cần scaffold từ đầu và xây **toàn bộ đề tài** (cả yêu cầu trung bình lẫn khá).

**Quyết định đã chốt:**
- Phạm vi: toàn bộ đề tài (đặt vé + tích điểm + phân hạng + voucher + ưu đãi cá nhân hóa + chiến dịch khuyến mãi)
- Frontend: Vue 3 + Vite + Pinia + Vue Router + axios + **Tailwind CSS**
- Thanh toán: **VNPay sandbox** (thiết kế sau interface `PaymentService`, có `MockPaymentService` dự phòng qua property `payment.provider=vnpay|mock`)
- Schema: **Flyway migration** (SQL Server), `ddl-auto=validate`

**Môi trường đã kiểm tra:** Java 21 LTS, Node 26, npm 11, không có Maven global (dùng `.\mvnw.cmd`), SQL Server Express `localhost\SQLEXPRESS` đang chạy, có sqlcmd.

**Cấu trúc:** monorepo `backend/` (Spring Boot 3.x, group `com.linhvecac`) + `frontend/` (Vite + Vue). Tiền VND lưu `BIGINT`. Text tiếng Việt dùng `NVARCHAR` + literal `N'...'`.

## 8 Phase — mỗi phase kết thúc chạy được

### P1 — Scaffold + kết nối DB + Flyway baseline ✅ HOÀN THÀNH
- [x] Backend từ Spring Initializr (Boot **3.5.16**, tải zip): deps `web, data-jpa, security, validation, flyway, sqlserver, lombok`; đã thêm tay `jjwt-api/impl/jackson` (0.12.6); `flyway-sqlserver` có sẵn từ Initializr.
- [x] Frontend: `npm create vite@latest frontend -- --template vue`, thêm `pinia vue-router axios tailwindcss @tailwindcss/vite`; proxy `/api → http://localhost:8080` trong `vite.config.js`.
- [x] Tạo DB `linhvecac` trên SQLEXPRESS + SQL login `linhvecac_app` qua `backend/db/00_bootstrap.sql` (Mixed Mode đã bật sẵn). **Dev hiện dùng `sa` trong application-local**.
- [x] `application.properties` (placeholder env) + `application-local.properties` (gitignored). JDBC dùng **port tĩnh 1433 trực tiếp** (`jdbc:sqlserver://localhost:1433;databaseName=linhvecac;encrypt=true;trustServerCertificate=true`) — KHÔNG dùng `instanceName` vì SQL Browser tắt (xem Rủi ro #1).
- [x] `V1__init.sql` baseline (bảng `app_meta`); `GET /api/health` (kiểm DB thật); git init + .gitignore; `SecurityConfig` tạm `permitAll`.
- **Kiểm tra:** ✅ backend boot + Flyway apply V1 (flyway_schema_history: `1/init/success`); `npm run dev` (5173) → `/api/health` qua proxy trả `{status:UP, db:UP, app:"Linh Vé Các"}` HTTP 200.

### P2 — Auth + đăng ký thành viên ✅ HOÀN THÀNH
- [x] `V2__users.sql`, `V3__seed_admin.sql` (admin dev: `admin@linhvecac.vn` / `Admin@123456`). Spring Security + JWT filter (jjwt 0.12.6, HS256, secret qua `app.jwt.secret` trong application-local), role USER/ADMIN, BCrypt. Kèm `GlobalExceptionHandler` + `ErrorResponse` JSON tiếng Việt thống nhất.
- [x] API: `POST /api/auth/register` (trả token luôn), `POST /api/auth/login`, `GET|PUT /api/users/me`; `/api/admin/**` chặn `hasRole('ADMIN')` sẵn cho P3.
- [x] FE: trang login/register/hồ sơ (`/dang-nhap`, `/dang-ky`, `/tai-khoan`), `authStore` (token localStorage key `lvc_token`), axios interceptor gắn Bearer + auto-logout khi 401, route guard `requiresAuth`/`guestOnly` với `?redirect=`. Dựng nền UI: font Be Vietnam Pro self-host, `ui/BaseButton|BaseInput|ErrorState`, `AppHeader`.
- **Kiểm tra:** ✅ 12 case API PASS (register/409 trùng email/400 validation/login/401 sai mật khẩu/me/PUT me/admin ADMIN/403 USER vào admin); 12 bước E2E Playwright qua UI thật PASS (đăng ký → đăng xuất → guard redirect → login sai hiện lỗi → login đúng quay lại `redirect` → sửa hồ sơ → F5 giữ phiên → token rác bị nhả về login → admin seed login); `mvnw test` xanh, `npm run build` sạch.

### P3 — Catalog + admin CRUD ✅ HOÀN THÀNH
- [x] `V4__catalog.sql` (regions, cinema_chains, cinemas, rooms, seats, movies, showtimes, concessions) + `V5__seed_catalog.sql` (2 khu vực, 2 hãng, 3 rạp × 2 phòng, lưới ghế 8×10 mọi phòng, 6 phim, suất chiếu 7 ngày neo theo ngày hiện tại, 6 món bắp nước). Ảnh: **upload file** (`app.upload.dir`, phục vụ `/api/files/**`).
- [x] Module `com.linhvecac.catalog.{region,cinema,movie,showtime,concession}` (Entity/Repository/Service/Controller/dto record). API public đọc catalog (permitAll GET) + API admin CRUD `/api/admin/...` (`hasRole('ADMIN')`), gồm `POST /api/admin/rooms/{id}/seats/generate` + `PUT .../seats` đổi loại ghế; overlap suất chiếu → 409; `PageResponse<T>` cho list phim; `POST /api/admin/uploads` (multipart, ≤5MB).
- [x] FE: trang chủ "phim đang chiếu" (P-01) + chi tiết phim/tab 7 ngày (P-02); khu admin light-theme (`AdminLayout` + guard `requiresAdmin`) với `CrudTable` + modal form cho phim (upload poster)/lịch chiếu/rạp-phòng/bắp nước + sơ đồ ghế (P-12). UI mới: `utils/format`, `ui/{BaseSelect,BaseModal,BaseBadge,LoadingState,EmptyState}`, `MovieCard`, `admin/{CrudTable,ImageUploader}`.
- **Kiểm tra:** ✅ `mvnw test` xanh (overlap→409, sinh ghế; full context boot + Flyway V4/V5 + `ddl-auto=validate`); API: USER→403 / ADMIN→200, admin tạo phim+suất → hiện ở public, overlap→409, sinh 15 ghế (3×5); 8 bước E2E Playwright PASS (home 9 card, chi tiết phim, login admin, dashboard, tạo phim + upload poster hiện ở bảng & public, mở sơ đồ ghế, user thường bị chặn `/admin`); `npm run build` sạch.

### P4 — Luồng đặt vé 6 bước + giữ ghế (lõi concurrency) ✅ HOÀN THÀNH
- [x] `V6__booking.sql`: bookings, **booking_seats** (guard chống trùng ghế), booking_concessions. `ticket_code` dùng filtered unique index (UNIQUE thường của SQL Server chỉ cho 1 NULL); nhả ghế = DELETE row.
- [x] `booking_seats`: `UNIQUE(showtime_id, seat_id)`, `status HOLD|CONFIRMED`, `hold_expires_at`, cột `ticket_code` (kiêm luôn entity vé), `booking_id` NULLABLE (hold tạo trước đơn). Hold = 1 transaction: DELETE hold hết hạn → INSERT HOLD (TTL 10 phút, hold sau kế thừa hạn hold trước) → `saveAllAndFlush` bắt `DataIntegrityViolationException` → 409 "Ghế đã có người giữ". **Gotcha đã gặp:** query `@Modifying(clearAutomatically)` phải chạy TRƯỚC khi load entity dùng lại sau đó, không thì proxy LAZY bị detach → LazyInitializationException 500. Job 60s (`BookingCleanupJob` + `SchedulingConfig @EnableScheduling`) chuyển booking quá hạn → EXPIRED rồi xóa HOLD hết hạn. `PricePolicy`: STANDARD = base, VIP = base + 30.000, COUPLE = base × 2.
- [x] API: `POST/DELETE /api/bookings/hold`, `GET /api/showtimes/{id}/seats` (AVAILABLE/HELD/BOOKED/MINE, cần đăng nhập), `POST /api/bookings` (PENDING_PAYMENT, ghế lấy từ hold sống của user, expires_at = +10' đồng bộ vào hold), `GET /api/bookings/quote?concessions=id:qty`, `GET /api/cinemas/{id}/showtimes?date=` (public, bước 3 wizard). Không cần sửa SecurityConfig.
- [x] FE: wizard 6 bước `/dat-ve/khu-vuc → rap → suat-chieu → ghe → bap-nuoc → thanh-toan` (nested routes + `BookingLayout`), guard `meta.step` chuyển về `firstIncompleteStep`; `bookingStore` (Pinia options, persist sessionStorage `lvc_booking` tự viết) + `SeatMap.vue` (poll 10s, max 8 ghế, toast khi 409) + `CountdownBadge.vue` + `BookingStepper.vue`; MovieDetailPage bấm suất → điền sẵn bước 1–3, vào thẳng bước ghế. Bước 6 (P4) dừng ở tạo đơn + badge "Chờ thanh toán", nút VNPay disabled chờ P5.
- **Kiểm tra:** ✅ 20 unit test BE xanh (hold 409/max 8/suất đóng/TTL kế thừa, create snapshot tiền, cleanup đúng thứ tự); 12 case API PASS (kể cả 2 hold song song ×5 vòng → đúng 1 thành công 1 nhận 409); job dọn: hold quá hạn nhả ghế + đơn quá hạn → EXPIRED (backdate DB, chờ job 60s); 10 bước E2E Playwright PASS (deep entry từ chi tiết phim, giữ ghế + countdown, F5 giữ state, nhảy cóc bị đẩy về bước 1, đi trọn wizard, user B thấy ghế bị giữ disabled); `npm run build` sạch.

### P5 — Thanh toán VNPay + vé ✅ HOÀN THÀNH
- [x] `V7__payments.sql` (payments: `txn_ref UNIQUE` = code+'-'+attempt, provider VNPAY/MOCK, status PENDING/SUCCESS/FAILED, các cột vnp_*). Slice `com.linhvecac.payment`: `PaymentService` (interface) → `VnPayPaymentService` / `MockPaymentService` chọn bằng `@ConditionalOnProperty payment.provider` (mặc định mock), `AbstractPaymentService` gom validate + tạo Payment; `PaymentProcessor` finalize dùng chung.
- [x] VNPay: `VnPaySigner` ký HMAC-SHA512 trên chuỗi **đã URL-encode** (US-ASCII, space→'+'), `vnp_Amount = total × 100`, giờ Asia/Ho_Chi_Minh, `vnp_ExpireDate = booking.expires_at`. Return URL (`GET /api/payments/vnpay/return` → 302 về FE) + IPN (`GET /api/payments/vnpay/ipn` → RspCode 00/01/02/04/97), cả hai qua `PaymentProcessor` nên idempotent. Mock: cổng giả lập tự render trang chọn Thành công/Hủy rồi finalize + redirect y hệt luồng thật.
- [x] `BookingService.markPaid(code)` transactional, guard-update `markPaidIfPending` (`WHERE status='PENDING_PAYMENT'`): PENDING_PAYMENT→PAID, HOLD→CONFIRMED + sinh `ticket_code` ("VE"+8). Hook TODO(P6) tích điểm/hạng, TODO(P7) voucher→USED. Hủy trên gateway → payment FAILED, đơn giữ PENDING để retry (attempt mới), job 60s expire nếu bỏ.
- [x] FE: nút "Thanh toán qua VNPay" redirect cổng; trang `/thanh-toan/ket-qua` (`PaymentResultPage`) hiện vé (mã text + QR qua `TicketQr.vue`, dep `qrcode`), nhánh success/failed(retry)/invalid; `api/payment.js`.
- **Kiểm tra:** ✅ 28 unit test BE xanh (`VnPaySignerTest` build/verify/tamper/encode; `PaymentProcessorTest` success/already-done/amount-mismatch/cancel/mock/not-found; `BookingServiceTest` markPaid idempotent + getByCode 404). API flow PASS: hold→booking→payment→mock success→PAID+sinh mã vé; fail→giữ PENDING; retry→txnRef `-2`; gọi lại success→idempotent giữ nguyên mã vé; GET đơn không tồn tại→404. E2E Playwright PASS: qua cổng mock (proxy Vite→backend→302) landing `/thanh-toan/ket-qua?status=success`, 2 vé + 2 QR render, đơn PAID. `mvnw test` xanh, `npm run build` sạch. **Gotcha:** VS Code Java extension biên dịch đè `target/classes` mất cờ `-parameters` → 500 khi resolve `@PathVariable`; fix bằng `mvnw clean compile` trước khi `spring-boot:run`. VNPay sandbox thật (thẻ NCB `9704198526191432198`) chờ user đổi `PAYMENT_PROVIDER=vnpay` (credentials đã có sẵn trong application-local).

### P6 — Tích điểm + phân hạng
- [ ] `V9__loyalty.sql`: point_transactions, tier_history; thêm `points_balance`, `lifetime_points`, `tier` vào users. (V8 đã dùng cho ảnh poster/backdrop phim.)
- [ ] Quy tắc: **1 điểm / 10.000 ₫** (`floor(total/10000)`), tích trong cùng transaction markPaid. Hạng theo **lifetime_points** (SILVER 0+, GOLD ≥ 5.000, PLATINUM ≥ 15.000) — hàm thuần, không cần job hạ hạng, dễ bảo vệ trước hội đồng; đổi điểm không tụt hạng. Ngưỡng đặt trong `TierPolicy`.
- [ ] API: `GET /api/loyalty/summary`, `GET /api/loyalty/points-history`. FE: tab điểm thưởng + `TierProgressBar.vue`.
- **Kiểm tra:** thanh toán 120.000 ₫ → +12 điểm; user seed sát ngưỡng thanh toán → lên hạng, có dòng tier_history.

### P7 — Voucher, chiến dịch KM, tự động áp dụng, ưu đãi cá nhân hóa
- [ ] `V10__promotions.sql` (campaigns, vouchers, user_vouchers) + `V11__seed_promotions.sql`.
- [ ] Voucher: PERCENT (cap `max_discount_amount`) / FIXED; ràng buộc `min_order_amount`, `valid_from/to`, `min_tier`, `points_cost` (đổi điểm), `quantity`.
- [ ] **Auto-apply** `VoucherService.findBestVoucher(user, subtotal)`: lọc voucher AVAILABLE hợp lệ → tính discount từng cái → chọn max, tie-break voucher sắp hết hạn trước; user có thể override/bỏ. Booking EXPIRED/CANCELLED → voucher hoàn về AVAILABLE.
- [ ] **OfferEngine rule-based** (cron 6h sáng + hook sau thanh toán, idempotent theo campaign/kỳ): sinh nhật (15%), thể loại/rạp yêu thích 90 ngày, win-back >60 ngày không mua (20%), quà lên hạng. Có `POST /api/admin/offers/run` để demo trực tiếp khi bảo vệ.
- [ ] Admin: CRUD campaigns/vouchers, danh sách thành viên theo hạng.
- **Kiểm tra:** 2 voucher hợp lệ → quote chọn cái giảm nhiều hơn; voucher dưới min_order bị bỏ qua; chạy offers 2 lần chỉ cấp 1 voucher sinh nhật.

### P8 — Lịch sử, dashboard, hoàn thiện
- [ ] Member: lịch sử giao dịch/điểm/voucher có filter. Admin dashboard: doanh thu theo ngày, top phim, phân bố hạng.
- [ ] Polish: rà chữ tiếng Việt, empty/loading/error state, responsive; README hướng dẫn chạy; test cho `LoyaltyService`, `VoucherService` (chọn best voucher), seat-hold conflict.
- **Kiểm tra:** số liệu dashboard khớp query tay trong SSMS; `.\mvnw.cmd test` xanh; `npm run build` sạch.

## Schema chính (Flyway V1→V11, `backend/src/main/resources/db/migration/`; V8 = ảnh poster/backdrop phim)

users · regions · cinema_chains · cinemas · rooms · seats (`UNIQUE(room_id,row_label,col_number)`, loại STANDARD/VIP/COUPLE) · movies · showtimes (`base_price`, index theo room/movie+starts_at) · concessions · bookings (code, status, subtotal/discount/total, `expires_at`, index `(status,expires_at)`) · **booking_seats** (`UNIQUE(showtime_id,seat_id)` — chốt chặn race condition) · booking_concessions · payments (vnp_txn_ref UNIQUE) · point_transactions (delta có dấu, EARN/REDEEM/ADJUST) · tier_history · campaigns · vouchers · user_vouchers.

## Cấu trúc code

**Backend** `com.linhvecac.{config, common, auth, user, catalog.{region,cinema,movie,showtime,concession}, booking, payment, loyalty, promotion, admin}` — mỗi module: Entity/Repository/Service/Controller/dto; entity dùng `GenerationType.IDENTITY`.

**Frontend** `src/{api (http.js + wrapper theo module), stores/{auth,booking,catalog}.js, router, layouts/{Default,Admin}Layout.vue, pages, components}` — component chủ chốt: `SeatMap`, `BookingStepper`, `CountdownBadge`, `CheckoutSummary`, `VoucherCard`, `TierProgressBar`, admin `CrudTable`.

## Rủi ro Windows + SQLEXPRESS (xử lý ở P1)

1. ✅ ĐÃ XỬ LÝ (P1): SQLEXPRESS đã bật TCP/IP + cấu hình **port tĩnh 1433**, nhưng SQL Browser tắt → JDBC không phân giải được `instanceName` (timeout UDP 1434). Giải pháp đã dùng: JDBC nối thẳng `localhost:1433`, bỏ `instanceName`. (Nếu máy khác dùng dynamic port thì bật SQL Browser hoặc pin 1433.)
2. mssql-jdbc ≥10 mặc định `encrypt=true` → phải thêm `trustServerCertificate=true`.
3. SQLEXPRESS mặc định chỉ Windows auth → bật Mixed Mode, tạo SQL login `linhvecac_app`.
4. Flyway 10 cần module `flyway-sqlserver` riêng; migration không dùng `GO`/`USE`.
5. Text tiếng Việt: NVARCHAR + `N'...'`, file .sql lưu UTF-8, `spring.flyway.encoding=UTF-8`.
6. Múi giờ: lưu DATETIME2 giờ VN nhất quán; `vnp_CreateDate` phải giờ VN.
7. Port 8080 có thể bị chiếm → document `server.port`.

## Kiểm tra tổng thể

Sau mỗi phase chạy demo như mô tả. Kết thúc: luồng đầy đủ đăng ký → đặt vé 6 bước → VNPay sandbox → nhận vé + điểm → lên hạng → nhận ưu đãi → đặt vé lần 2 thấy voucher tự áp dụng → xem lịch sử. Cập nhật CLAUDE.md khi cấu trúc thực tế hình thành (P1) và bổ sung mục VNPay/loyalty khi hoàn thành P5–P7.
