# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Dự án

**Đề tài tốt nghiệp:** Xây dựng website bán vé xem phim trực tuyến tích hợp ưu đãi khách hàng thành viên — **"Linh Vé Các"**.

Trạng thái: **P7 hoàn thành** — monorepo scaffold + kết nối DB (P1); auth JWT hoàn chỉnh (P2): đăng ký/đăng nhập/hồ sơ, role USER/ADMIN, admin seed `admin@linhvecac.vn` / `Admin@123456` (dev); catalog + admin CRUD (P3); luồng đặt vé 6 bước + giữ ghế chống race condition (P4); thanh toán VNPay/mock + sinh vé (P5); tích điểm + phân hạng thành viên (P6); voucher + đổi điểm + auto-apply + OfferEngine cá nhân hóa + admin campaign/thành viên (P7). Phase còn lại (P8) theo `PLAN.md`. Cập nhật file này khi cấu trúc thực tế thay đổi.

**Chi tiết môi trường đã dựng (P1):**
- Backend: Spring Boot **3.5.16**, Java 21, group `com.linhvecac`. Deps: web, data-jpa, security, validation, flyway-core, flyway-sqlserver, mssql-jdbc, lombok, jjwt 0.12.6.
- Frontend: Vite 8 + Vue 3.5, Pinia 3, Vue Router 5, axios, Tailwind CSS v4 (`@tailwindcss/vite`). Design tokens trong `frontend/src/assets/main.css` (`@theme`); font Be Vietnam Pro self-host trong `frontend/src/assets/fonts/`.
- DB: SQL Server SQLEXPRESS, DB `linhvecac`. **JDBC nối thẳng `localhost:1433`** (không dùng `instanceName` vì SQL Browser tắt). Dev dùng login `sa` trong `application-local.properties` (gitignored); script tạo DB + login riêng ở `backend/db/00_bootstrap.sql`.
- Kết nối local KHÔNG commit: `backend/src/main/resources/application-local.properties` (đã gitignore) — chứa `DB_URL/DB_USER/DB_PASSWORD` và `JWT_SECRET` (Base64 ≥ 32 bytes, map vào `app.jwt.secret`).

**Auth (P2):** JWT HS256 stateless — `JwtAuthenticationFilter` nạp `User` từ DB làm principal (`@AuthenticationPrincipal User`), authority `ROLE_USER|ROLE_ADMIN`; `/api/auth/**` + `/api/health` permitAll, `GET` catalog (`/api/regions|cinemas|movies|concessions|files/**`) permitAll, `/api/admin/**` cần ADMIN, còn lại authenticated. Lỗi trả JSON tiếng Việt thống nhất qua `GlobalExceptionHandler`/`ErrorResponse` (`{message, errors}`); lỗi nghiệp vụ ném `ApiException(HttpStatus, message)`. FE: token localStorage `lvc_token`, interceptor trong `src/api/http.js` (gắn Bearer, auto-logout 401), guard `requiresAuth`/`guestOnly` trong router; component chuẩn tại `src/components/ui/`.

**Catalog + admin (P3):** BE feature-slice `com.linhvecac.catalog.{region,cinema(chain/cinema/room/seat),movie,showtime,concession}` — mỗi module Entity/Repository/Service/Controller + `dto` record; enum lưu string mirror CHECK (`MovieStatus`, `SeatType`, `ShowtimeStatus`, `ConcessionCategory`). Public GET đọc catalog; admin CRUD `/api/admin/**`. Nghiệp vụ chính: **overlap suất chiếu** (`ShowtimeRepository.countOverlapping`, `ends_at = starts_at + duration + 15'`) → 409; **sinh lưới ghế** (`POST /api/admin/rooms/{id}/seats/generate`, xóa-ghi trong 1 transaction) + `PUT .../seats` đổi loại; phân trang `common/PageResponse<T>`. **Upload ảnh**: `POST /api/admin/uploads` (multipart ≤5MB → `app.upload.dir`, mặc định `backend/uploads/` gitignored), phục vụ lại `/api/files/**` qua `FileStorageConfig`. Migration `V4__catalog.sql` (schema) + `V5__seed_catalog.sql` (suất chiếu neo `SYSDATETIME()` để luôn có suất 7 ngày tới). FE: `layouts/AdminLayout.vue` (light theme, guard `requiresAdmin`, `App.vue` ẩn header khi `route.meta.admin`), `components/admin/{CrudTable,ImageUploader}`, form admin dạng **modal**; `utils/format.js` (`formatVnd/formatDate/formatTime`); UI thêm `ui/{BaseSelect,BaseModal,BaseBadge,LoadingState,EmptyState}`. API client tách theo module `src/api/{catalog,movies,admin}.js`.

**Đặt vé + giữ ghế (P4):** BE slice `com.linhvecac.booking` — `Booking` (PENDING_PAYMENT/PAID/EXPIRED/CANCELLED, `expires_at`), `BookingSeat` (kiêm vé: HOLD/CONFIRMED, `booking_id` NULLABLE khi mới giữ, `hold_expires_at`, `ticket_code` filtered unique index), `BookingConcession` (snapshot `unit_price`). **Chống trùng ghế bằng `UNIQUE(showtime_id, seat_id)` ở DB**: hold = dọn hold hết hạn → INSERT → `saveAllAndFlush` bắt `DataIntegrityViolationException` → 409 "Ghế đã có người giữ"; nhả ghế = DELETE row. TTL hold 10' (`BookingService.HOLD_TTL_MIN`), hold sau kế thừa hạn hold trước; tối đa 8 ghế/user/suất. **Gotcha:** query `@Modifying(clearAutomatically)` phải chạy trước khi load entity sẽ dùng lại — không thì proxy LAZY detach → `LazyInitializationException`. Giá ghế tính DUY NHẤT server-side qua `PricePolicy` (STANDARD = base, VIP = +30.000, COUPLE = ×2), snapshot vào `booking_seats.price`. Job `BookingCleanupJob` 60s (`SchedulingConfig` bật `@EnableScheduling`): đơn quá hạn → EXPIRED rồi xóa HOLD hết hạn (nhả ghế). API: `POST/DELETE /api/bookings/hold`, `GET /api/showtimes/{id}/seats` (AVAILABLE/MINE/HELD/BOOKED theo góc nhìn user, cần đăng nhập), `GET /api/bookings/quote?concessions=id:qty`, `POST /api/bookings` (ghế lấy từ hold sống, không nằm trong request; code `LVC` + 8 ký tự), `GET /api/cinemas/{id}/showtimes?date=` (public, gộp theo phim). FE: wizard `/dat-ve/{khu-vuc,rap,suat-chieu,ghe,bap-nuoc,thanh-toan}` nested trong `pages/booking/BookingLayout.vue` (requiresAuth, `meta.step`, guard đẩy về `firstIncompleteStep`); `stores/booking.js` (options store, persist sessionStorage `lvc_booking` tự viết — action `persist()`); `components/{BookingStepper,CountdownBadge,SeatMap}.vue` (SeatMap poll 10s, toast qua `inject('bookingToast')` từ layout); `api/booking.js`; `formatCountdown` trong `utils/format.js`; MovieDetailPage bấm suất → `enterFromMovie` điền bước 1–3 rồi vào thẳng `/dat-ve/ghe`. Bước thanh toán P4 dừng ở tạo đơn (nút VNPay disabled — P5).

**Thanh toán + vé (P5):** BE slice `com.linhvecac.payment` — `Payment` (`V7__payments.sql`, `txn_ref UNIQUE` = `booking.code + '-' + attempt` vì VNPay dedup theo ref → retry cần ref mới; provider VNPAY/MOCK, status PENDING/SUCCESS/FAILED, cột vnp_*). `PaymentService` (interface) có 2 impl chọn bằng `@ConditionalOnProperty payment.provider` (`MockPaymentService` mặc định `matchIfMissing`, `VnPayPaymentService` khi =vnpay); `AbstractPaymentService.initiate` gom validate (đơn của user, còn PENDING_PAYMENT, chưa hết hạn) + tạo Payment PENDING; `PaymentProcessor` (KHÔNG conditional) finalize dùng chung. **`VnPaySigner` gotcha kinh điển:** ký HMAC-SHA512 trên chuỗi query **đã URL-encode** (US-ASCII, space→'+'), sort key; verify callback phải re-encode param đã bị servlet decode rồi mới so hash. Params VNPay 2.1.0: `vnp_Amount = total×100`, `vnp_CreateDate` giờ `Asia/Ho_Chi_Minh`, `vnp_ExpireDate = booking.expiresAt`. `POST /api/payments` (auth) → `PaymentInitResponse{payUrl,txnRef}`; `GET /api/payments/vnpay/return` (permitAll) verify+finalize → 302 `{app.frontend-url}/thanh-toan/ket-qua?code=&status=success|failed|invalid`; `GET /api/payments/vnpay/ipn` (permitAll) → JSON RspCode 00/01/02/04/97; cả hai qua `PaymentProcessor` nên **idempotent** (guard `payment.status==PENDING` + guard-update `markPaidIfPending`). Mock (`GET /api/payments/mock/pay`, permitAll): render trang HTML chọn Thành công/Hủy → finalize + 302 y hệt luồng thật. **`BookingService.markPaid(code)`**: guard-update `WHERE status='PENDING_PAYMENT'` → PAID, ghế HOLD→CONFIRMED + sinh `ticket_code` ("VE"+8, `existsByTicketCode` + filtered unique index), có hook `// TODO(P6)` tích điểm/hạng và `// TODO(P7)` voucher→USED; `GET /api/bookings/{code}` owner-guard 404. Hủy trên cổng → payment FAILED, đơn giữ PENDING để retry (attempt mới, TTL còn lại), `BookingCleanupJob` expire nếu bỏ. Config: `payment.provider` + `app.frontend-url` + `app.vnpay.{tmn-code,hash-secret,pay-url,return-url}` trong `application.properties` (secret thật trong `application-local.properties` gitignored, giống `JWT_SECRET`). FE: `api/payment.js` (`createPayment`), `api/booking.js` thêm `getBooking(code)`, `StepCheckout.vue` bật nút → `window.location.href = payUrl`, `pages/PaymentResultPage.vue` (route `/thanh-toan/ket-qua`, requiresAuth, ngoài `/dat-ve`) + `components/TicketQr.vue` (dep `qrcode`), nhánh success/failed(retry)/invalid. **Gotcha môi trường:** VS Code Java extension biên dịch đè `target/classes` mất cờ `-parameters` → 500 khi Spring resolve `@PathVariable`; fix bằng `mvnw clean compile` trước `spring-boot:run`.

**Khu tài khoản (bổ sung sau P5):** `GET /api/bookings` (đơn của user, mới nhất trước, `BookingSummaryResponse` + `findMineWithDetails` fetch-join) và `PUT /api/users/me/password` (`UserService.changePassword` verify mật khẩu cũ qua `PasswordEncoder`, guard khác mật khẩu cũ). FE: `layouts/AccountLayout.vue` (tab Hồ sơ · Vé của tôi · Đổi mật khẩu), route `/tai-khoan/{ho-so,ve-cua-toi,doi-mat-khau}` nested (`account-*`), `pages/account/{MyBookingsPage,ChangePasswordPage}.vue`; "Vé của tôi" đơn PAID → bấm "Xem vé" điều hướng `payment-result`. Tab voucher để P7. **Chặn ADMIN đặt vé:** luồng mua (`hold/release/quote/create` của BookingController, seat map, `POST /api/payments`) gắn `@PreAuthorize("hasRole('USER')")`; `AccessDeniedException` từ method security ném sau filter chain nên `GlobalExceptionHandler` thêm handler trả 403 (không thì thành 500). FE: route `/dat-ve` + `/thanh-toan/ket-qua` thêm `meta.userOnly` → guard đẩy admin về `/admin`; MovieDetailPage ẩn/disable nút suất chiếu + báo cảnh báo khi `isAdmin`; AccountLayout ẩn tab "Vé của tôi" với admin.

**Tích điểm + phân hạng (P6):** BE slice `com.linhvecac.loyalty` — `V9__loyalty.sql` thêm `points_balance`/`lifetime_points`/`tier` vào `users` + bảng `point_transactions` (ledger delta có dấu, type EARN/REDEEM/ADJUST, `balance_after`) + `tier_history`. **Gotcha migration:** CHECK cho `tier` phải khai INLINE cùng cột trong `ALTER TABLE users ADD ...` — tách thành `ADD CONSTRAINT ck_users_tier CHECK (tier IN ...)` riêng thì SQL Server phân giải tên trong cùng batch trước khi cột tồn tại → lỗi "Invalid column name 'tier'". `enum Tier{SILVER,GOLD,PLATINUM}` trong `com.linhvecac.user` (cạnh `Role`, mirror CHECK). `TierPolicy` (thuần): `tierFor(lifetimePoints)` + `nextTier`/`pointsToNextTier`; **ngưỡng demo-friendly GOLD ≥ 100, PLATINUM ≥ 300** (thấp hơn con số gốc 5.000/15.000 trong PLAN.md để lên hạng tự nhiên khi demo). `LoyaltyService.awardForBooking(user, bookingId, total)`: điểm `= total/10.000` (`pointsFor`, bỏ qua nếu ≤0), cộng cả 2 bộ đếm, ghi 1 dòng EARN, xét hạng lại — nếu đổi thì set `user.tier` + ghi `tier_history`; gọi tại `BookingService.markPaid` (thay hook `TODO(P6)`) trong cùng transaction nên **tích đúng 1 lần/đơn** nhờ guard `markPaidIfPending`. `PointTransaction` lưu `userId`/`bookingId` dạng `Long` (không quan hệ JPA) để loyalty không phụ thuộc ngược slice booking. API `GET /api/loyalty/{summary,points-history}` (`@PreAuthorize hasRole('USER')`); `UserResponse`/`GET /api/users/me` trả thêm `pointsBalance/lifetimePoints/tier`; `BookingResponse.earnedPoints` (chỉ khi PAID, dùng chung `LoyaltyService.pointsFor`). FE: `api/loyalty.js`, `pages/account/PointsPage.vue` (điểm text-4xl + `components/TierProgressBar.vue` + quyền lợi 3 hạng + lịch sử điểm), route/tab `account-points` (`userOnly`), badge hạng (`BaseBadge` thêm variant `platinum` = indigo-300) ở header `AccountLayout`, `utils/tier.js` (label/variant/benefits + `TIER_ORDER`); `PaymentResultPage` hiện "+X điểm" và gọi `auth.fetchMe()` làm mới điểm/hạng sau thanh toán. Đổi điểm→voucher là P7 (REDEEM trừ `points_balance`, không đụng lifetime).

**Voucher + ưu đãi cá nhân hóa (P7):** BE slice `com.linhvecac.promotion` — **mô hình 2 bảng** (`V10__promotions.sql`): `campaigns` (định nghĩa quy tắc: `voucher_type` PERCENT/FIXED, `discount_value`, `max_discount_amount`, `min_order_amount`, `min_tier`, `points_cost`, `trigger_type` MANUAL/REDEEM/BIRTHDAY/WINBACK/GENRE_FAVORITE/TIER_UP, `valid_days`, `quantity`, `per_user_limit`, `is_active`) + `user_vouchers` (phiếu người dùng sở hữu: `code` unique, status AVAILABLE/RESERVED/USED/EXPIRED, `valid_from/to`, `booking_id`, `period_key`). **`userId`/`bookingId` để `Long` thuần** (không quan hệ JPA) như `PointTransaction` để promotion không phụ thuộc ngược booking; chỉ `campaign` là `@ManyToOne`. Idempotency OfferEngine chốt ở DB bằng **filtered unique index** `ux_user_vouchers_grant(campaign_id,user_id,period_key) WHERE period_key IS NOT NULL` (REDEEM/MANUAL để `period_key` NULL nên không bị chặn). `V11__seed_promotions.sql`: 2 campaign REDEEM (points_cost 20/30) + 4 template OfferEngine. `birth_date` đã có từ V2 (form hồ sơ đã surface). **`VoucherService`** (nguồn DUY NHẤT tính giảm giá): `static discountFor(campaign, subtotal)` thuần (PERCENT cap `max_discount_amount`, FIXED, dưới `min_order`→0, không vượt subtotal); `resolveForQuote(user, subtotal, voucherCode)` — `voucherCode` null→auto chọn phiếu lợi nhất (`findBest`: max discount, tie-break `valid_to` sớm hơn), có mã→validate phiếu đó, sentinel **`"NONE"`**→bỏ áp; `reserve` = guard-update `reserveIfAvailable` (409 nếu bị chiếm); `redeem` gọi `LoyaltyService.redeemPoints` (trừ `points_balance`, KHÔNG đụng `lifetime` → không tụt hạng, ghi ledger REDEEM). **`OfferEngine.runAll()`** (cron `0 0 6 * * *` qua `OfferEngineJob` + `POST /api/admin/offers/run`): 4 rule sinh voucher theo `period_key` (sinh nhật=năm, favorite/winback=tháng, tier-up=tên hạng), pre-check `existsBy...` + unique index. Tích hợp `BookingService`: `quote`/`create` thêm `voucherCode` → discount server-side (thay hardcode 0); `markPaid` → `voucherService.markUsedForBooking` (thay `TODO(P7)`); `cleanupExpired` → `releaseForExpiredBookings` + `expireOutdated`. `QuoteResponse`/`BookingResponse` thêm `voucherCode/voucherName`; `CreateBookingRequest` thêm `voucherCode`. API: `GET/POST /api/vouchers{,/redeemable,/redeem}` (`@PreAuthorize hasRole('USER')`), `/api/admin/campaigns` (CRUD), `/api/admin/offers/run`, `GET /api/admin/members?tier=` (`PageResponse`, slice `user`). **Gotcha `@Modifying` trong transaction dùng chung:** `markUsedForBooking` phải `flushAutomatically=true` — không thì `clearAutomatically` xóa thay đổi điểm/ghế chưa flush (auto-flush của Hibernate không flush entity khác bảng với query) → **mất điểm khi thanh toán**; ngược lại `reserveIfAvailable` **KHÔNG** được `clearAutomatically` vì `create()` còn dùng lại hold/booking đã nạp → detach → LazyInit. FE: `api/voucher.js`, `api/booking.js`/`admin.js` thêm voucher; `stores/booking.js` thêm `voucherSelection`(null/`NONE`/mã) + `appliedVoucher`; `StepCheckout.vue` khối voucher + modal chọn (dùng `components/VoucherCard.vue`); `pages/account/VouchersPage.vue` (ví + đổi điểm, route/tab `account-vouchers`); `pages/admin/{CampaignsPage,OffersPage}.vue` (CRUD + chạy offers + bảng thành viên theo hạng, menu 🎫/🎁); `utils/voucher.js` (`voucherDiscountLabel`, `VOUCHER_STATUS`). Đổi voucher cho campaign chưa dùng: **admin không xóa campaign đã phát voucher** (tắt thay vì xóa).

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
