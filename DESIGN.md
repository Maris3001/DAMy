# DESIGN.md — Quy ước thiết kế "Linh Vé Các"

Tài liệu này định nghĩa hệ thống thiết kế cho toàn bộ giao diện dự án (Vue 3 + Tailwind CSS) và bộ prompt chuẩn để sinh giao diện. Mọi trang/component mới phải tuân theo quy ước ở Phần 1; mọi prompt sinh UI phải nhúng kèm khối "Bối cảnh thiết kế" ở Phần 2.

---

## Phần 1 — Quy ước thiết kế

### 1.1. Định hướng thẩm mỹ

- **Chủ đề:** rạp chiếu phim ban đêm — nền tối, sang trọng, điểm nhấn vàng đồng (gợi màn nhung + ánh đèn rạp, hợp với tên "Linh Vé Các").
- **Khu người dùng:** dark theme cố định (không cần toggle sáng/tối).
- **Khu admin:** light theme, ưu tiên mật độ thông tin và khả năng đọc bảng số liệu.
- Không dùng gradient sặc sỡ, không quá 2 màu nhấn trên một màn hình.

### 1.2. Màu sắc (design tokens)

Khai báo trong `frontend/src/assets/main.css` bằng `@theme` của Tailwind v4:

```css
@theme {
  /* Nền khu người dùng (dark) */
  --color-surface-950: #0B0D12;   /* nền trang */
  --color-surface-900: #12151D;   /* nền section */
  --color-surface-800: #1A1E29;   /* card, panel */
  --color-surface-700: #252A38;   /* hover, viền nổi */

  /* Màu nhấn chính — vàng đồng */
  --color-brand-400: #E5C56B;     /* hover */
  --color-brand-500: #D4AF37;     /* nút chính, giá tiền, điểm nhấn */
  --color-brand-600: #B8942C;     /* active/pressed */

  /* Màu phụ — đỏ màn nhung, dùng tiết chế */
  --color-crimson-500: #C0392B;   /* badge "HOT", cảnh báo hủy vé */

  /* Chữ trên nền tối */
  --color-ink-100: #F2F0EB;       /* tiêu đề */
  --color-ink-300: #C9C6BE;       /* chữ thường */
  --color-ink-500: #8B8878;       /* chữ phụ, placeholder */

  /* Ngữ nghĩa */
  --color-success: #2E9E5B;
  --color-warning: #D97706;
  --color-danger:  #DC2626;
  --color-info:    #2563EB;
}
```

**Màu hạng thành viên** (dùng nhất quán mọi nơi hiển thị tier):

| Hạng | Màu | Tailwind gợi ý |
|---|---|---|
| Silver | bạc `#9CA3AF` | `text-gray-400 / border-gray-400` |
| Gold | vàng `#D4AF37` | `text-brand-500 / border-brand-500` |
| Platinum | trắng ánh xanh `#A5B4FC` | `text-indigo-300 / border-indigo-300` |

**Màu trạng thái ghế** (SeatMap — bắt buộc thống nhất, có chú giải kèm mọi sơ đồ ghế):

| Trạng thái | Hiển thị |
|---|---|
| Trống (AVAILABLE) | viền `surface-700`, nền trong suốt |
| Đang chọn (SELECTED) | nền `brand-500`, chữ đen |
| Người khác giữ (HELD) | nền `surface-700`, chữ mờ, `cursor-not-allowed` |
| Đã bán (BOOKED) | nền `surface-800`, icon ✕, mờ 50% |
| VIP | như trống nhưng viền `brand-500` |
| Ghế đôi (COUPLE) | rộng gấp đôi, viền `crimson-500` |

### 1.3. Chữ (typography)

- **Font:** `Be Vietnam Pro` (hỗ trợ tiếng Việt tốt) — self-host trong `frontend/src/assets/fonts/`, fallback `system-ui, sans-serif`. Số liệu tiền/điểm dùng `tabular-nums`.
- **Thang chữ:** chỉ dùng các cỡ `text-sm` (14 — chữ phụ, bảng), `text-base` (16 — mặc định), `text-lg` (18 — tên phim trong card), `text-2xl` (24 — tiêu đề trang), `text-4xl` (36 — hero). Không dùng cỡ ngoài thang.
- **Tiền tệ:** luôn định dạng `120.000 ₫` (dấu chấm ngăn cách nghìn, ký hiệu ₫ sau số, cách 1 space). Viết 1 hàm `formatVnd()` trong `src/utils/format.js` và dùng ở mọi nơi.
- **Ngày giờ:** `Thứ 7, 12/07/2026` và `19:30`. Không dùng AM/PM.

### 1.4. Khoảng cách, bo góc, đổ bóng

- **Spacing:** theo thang 4px của Tailwind; padding card = `p-4` (mobile) / `p-6` (desktop); khoảng cách giữa section = `py-12`.
- **Bo góc:** `rounded-lg` cho card/nút, `rounded-md` cho input/ghế, `rounded-full` cho badge/avatar. Không trộn kiểu bo trong cùng một nhóm.
- **Bóng:** trên nền tối KHÔNG dùng shadow — phân lớp bằng màu nền (`surface-800` trên `surface-900`) và viền `border-white/5`. Admin (nền sáng) dùng `shadow-sm` cho card.
- **Container:** `max-w-6xl mx-auto px-4`.

### 1.5. Component chuẩn

Mỗi component dưới đây viết MỘT lần trong `src/components/ui/` rồi tái sử dụng — không chế lại kiểu mới:

- **BaseButton** — biến thể: `primary` (nền brand-500, chữ đen, hover brand-400), `secondary` (viền brand-500, chữ brand-500, nền trong suốt), `ghost` (chỉ chữ), `danger`. Cỡ `md` (h-10 px-4) và `lg` (h-12 px-6, dùng cho CTA đặt vé). Luôn có trạng thái `disabled` và `loading` (spinner thay chữ).
- **BaseInput / BaseSelect** — nền `surface-800`, viền `surface-700`, focus viền `brand-500`; label ở trên, lỗi màu `danger` dưới input.
- **BaseModal** — overlay `black/70`, panel `surface-800`, đóng bằng ESC + click overlay.
- **BaseBadge** — dùng cho hạng thành viên, trạng thái booking, nhãn thể loại phim.
- **EmptyState / LoadingState / ErrorState** — mọi danh sách gọi API bắt buộc có đủ 3 trạng thái này, kèm thông điệp tiếng Việt tự nhiên ("Chưa có suất chiếu nào cho ngày này").

### 1.6. Trạng thái booking (hiển thị nhất quán)

| Status | Nhãn tiếng Việt | Badge |
|---|---|---|
| PENDING_PAYMENT | Chờ thanh toán | warning |
| PAID | Đã thanh toán | success |
| EXPIRED | Hết hạn | gray |
| CANCELLED | Đã hủy | danger |

### 1.7. Responsive & tương tác

- Mobile-first; breakpoint dùng: `sm` 640, `md` 768, `lg` 1024. Sơ đồ ghế trên mobile cho phép cuộn ngang trong container riêng, không làm vỡ trang.
- Vùng chạm tối thiểu 40×40px (ghế trên mobile tối thiểu 28×28px + khoảng cách 4px).
- Transition duy nhất: `transition-colors duration-150`. Không animation trang trí phức tạp.
- Wizard đặt vé: thanh `BookingStepper` cố định trên đầu, luôn cho quay lại bước trước; nút tiếp tục `disabled` khi bước hiện tại chưa hợp lệ; đồng hồ đếm ngược giữ ghế (`CountdownBadge`) luôn hiển thị từ bước chọn ghế trở đi, chuyển màu `danger` khi còn < 2 phút.

### 1.8. Ngôn ngữ giao diện

- Toàn bộ UI tiếng Việt, giọng thân thiện, gọn ("Chọn ghế", "Tiếp tục", "Thanh toán ngay").
- Không viết hoa toàn bộ trừ badge ngắn (HOT, VIP).
- Lỗi hiển thị cho người dùng phải nói được cách xử lý: "Ghế A5 vừa có người chọn. Vui lòng chọn ghế khác."

---

## Phần 2 — Bộ prompt sinh giao diện

### Khối bối cảnh chung (BẮT BUỘC dán vào đầu mọi prompt sinh UI)

> Dự án "Linh Vé Các" — website đặt vé xem phim, Vue 3 `<script setup>` + Tailwind CSS v4. Dark theme cố định cho khu người dùng: nền `surface-950/900/800` (#0B0D12/#12151D/#1A1E29), màu nhấn vàng đồng `brand-500` (#D4AF37), chữ `ink-100/300/500`, KHÔNG dùng shadow trên nền tối (phân lớp bằng màu nền + viền `border-white/5`), bo góc `rounded-lg`, font Be Vietnam Pro, tiền định dạng `120.000 ₫`, toàn bộ chữ tiếng Việt. Tái sử dụng component trong `src/components/ui/` (BaseButton, BaseInput, BaseModal, BaseBadge, EmptyState/LoadingState/ErrorState). Mobile-first, container `max-w-6xl mx-auto px-4`. Chỉ dùng cỡ chữ text-sm/base/lg/2xl/4xl.

### P-01. Trang chủ

> [Khối bối cảnh chung] Tạo `pages/HomePage.vue`: (1) Hero banner phim nổi bật — poster nền phủ gradient đen từ dưới lên, tên phim text-4xl, nút "Đặt vé ngay" primary lg; (2) thanh chọn nhanh gồm 3 select (Khu vực, Rạp, Ngày) + nút tìm suất chiếu; (3) section "Phim đang chiếu" — grid poster 2 cột mobile / 4 cột lg, mỗi card: poster tỉ lệ 2:3 rounded-lg, badge độ tuổi (P/T13/T16/T18) góc trên trái, tên phim text-lg tối đa 2 dòng, thể loại + thời lượng ink-500 text-sm, hover hiện nút "Đặt vé"; (4) section "Sắp chiếu" tương tự nhưng card có badge ngày khởi chiếu thay nút đặt vé. Kèm LoadingState skeleton cho grid.

### P-02. Chi tiết phim

> [Khối bối cảnh chung] Tạo `pages/MovieDetailPage.vue`: cột trái poster lớn; cột phải tên phim text-2xl, hàng badge (độ tuổi, thể loại, thời lượng), mô tả ink-300, nút "Xem trailer" secondary (mở BaseModal chứa iframe YouTube) và "Đặt vé" primary lg. Bên dưới: khối chọn suất chiếu — hàng tab 7 ngày tới (thứ + ngày, tab active viền brand-500), dưới là danh sách rạp có suất, mỗi rạp một hàng: tên rạp + địa chỉ ink-500, các nút giờ chiếu dạng chip (viền surface-700, hover viền brand-500); bấm giờ chiếu → điều hướng vào luồng đặt vé. EmptyState khi ngày không có suất.

### P-03. Wizard đặt vé — khung + bước Khu vực/Rạp/Suất chiếu

> [Khối bối cảnh chung] Tạo layout wizard `pages/booking/BookingLayout.vue` chứa `BookingStepper` (6 bước: Khu vực → Rạp → Suất chiếu → Ghế → Bắp nước → Thanh toán; bước xong icon check màu brand-500, bước hiện tại chữ ink-100, bước chưa tới ink-500, mobile chỉ hiện số bước "Bước 2/6"), vùng nội dung router-view, và thanh đáy cố định: bên trái tổng tiền tạm tính text-lg brand-500, bên phải nút "Quay lại" ghost + "Tiếp tục" primary (disabled khi bước chưa hợp lệ). Tạo 3 bước đầu: chọn khu vực (grid card tỉnh/thành), chọn hãng & rạp (hàng logo hãng dạng tab tròn, dưới là danh sách card rạp: tên + địa chỉ + khoảng cách), chọn ngày & suất (tab 7 ngày + chip giờ chiếu nhóm theo phòng/định dạng). State đọc/ghi qua Pinia `bookingStore`.

### P-04. Bước chọn ghế (SeatMap)

> [Khối bối cảnh chung] Tạo `pages/booking/StepSeats.vue` + `components/SeatMap.vue`. Màn hình: thanh cong "MÀN HÌNH" phía trên (border-t-2 brand-500 bo cong, chữ ink-500 text-sm). Lưới ghế theo hàng A-J: nhãn hàng hai bên, ghế 28×28px mobile / 36×36px desktop, gap 4px, cuộn ngang trong container riêng trên mobile. Trạng thái ghế đúng quy ước DESIGN.md mục 1.2: trống viền surface-700; đang chọn nền brand-500 chữ đen; người khác giữ nền surface-700 mờ cursor-not-allowed; đã bán mờ 50% icon ✕; VIP viền brand-500; ghế đôi rộng gấp đôi viền crimson-500. Chú giải (legend) dưới sơ đồ. Panel phải (desktop) / thanh dưới (mobile): danh sách ghế đã chọn dạng chip có nút x, giá từng loại ghế, tổng tiền, CountdownBadge đếm ngược 10:00 giữ ghế (đỏ khi <2 phút). Chọn tối đa 8 ghế; bấm ghế đang giữ bởi người khác hiện toast "Ghế A5 vừa có người chọn". Poll sơ đồ mỗi 10 giây.

### P-05. Bước bắp nước

> [Khối bối cảnh chung] Tạo `pages/booking/StepConcessions.vue`: danh sách combo dạng hàng ngang — ảnh vuông 80px rounded-md, tên + mô tả ngắn ink-300, giá brand-500, stepper số lượng (nút − / số / nút +, min 0 max 10). Nhóm theo "Combo" và "Món lẻ". Có thể bỏ qua bước này (nút Tiếp tục luôn bật). Panel tóm tắt bên phải cập nhật tổng tiền vé + bắp nước theo thời gian thực.

### P-06. Bước xác nhận & thanh toán

> [Khối bối cảnh chung] Tạo `pages/booking/StepCheckout.vue`: card tóm tắt đơn — tên phim + poster nhỏ, rạp/phòng/suất, danh sách ghế, danh sách bắp nước, các dòng tiền: Tạm tính / Giảm giá (màu success, hiện tên voucher) / Tổng cộng text-2xl brand-500. Khối "Ưu đãi": mặc định hiện banner nền success/10 viền success "✓ Đã tự động áp dụng voucher tốt nhất: Giảm 15% sinh nhật (−18.000 ₫)" kèm link "Đổi voucher khác" mở BaseModal danh sách voucher khả dụng (card voucher: tên, điều kiện, hạn, radio chọn, mục "Không dùng voucher"). Voucher không đủ điều kiện hiện mờ kèm lý do. Dưới cùng: CountdownBadge + nút "Thanh toán qua VNPay" primary lg full-width (icon khóa), ghi chú nhỏ ink-500 "Bạn sẽ được chuyển đến cổng thanh toán VNPay".

### P-07. Trang kết quả thanh toán + vé

> [Khối bối cảnh chung] Tạo `pages/PaymentResultPage.vue` với 3 trạng thái: (1) Thành công — icon check tròn success lớn, "Đặt vé thành công!", card vé kiểu boarding-pass: viền đứt giữa, phần trên là phim/rạp/phòng/suất/ghế, phần dưới mã vé monospace text-2xl + mã QR placeholder, dòng điểm thưởng "+12 điểm Linh Vé" brand-500, nút "Xem vé của tôi" + "Về trang chủ"; (2) Thất bại/hủy — icon ✕ danger, lý do, nút "Thử lại"; (3) Đang xử lý — spinner. Vé phải chụp màn hình đẹp (dùng cho demo đồ án).

### P-08. Trang tài khoản (hồ sơ, điểm, voucher, lịch sử)

> [Khối bối cảnh chung] Tạo `pages/account/AccountLayout.vue` (sidebar trái: avatar + tên + BaseBadge hạng đúng màu Silver #9CA3AF / Gold #D4AF37 / Platinum #A5B4FC; menu Hồ sơ / Điểm thưởng / Ví voucher / Lịch sử đặt vé; mobile chuyển thành tab ngang) và 4 trang con: (1) Hồ sơ — form BaseInput họ tên/SĐT/ngày sinh, email readonly; (2) Điểm thưởng — card lớn: số điểm hiện có text-4xl brand-500, TierProgressBar thanh tiến độ tới hạng kế tiếp ("Còn 1.200 điểm nữa để lên Gold"), quyền lợi từng hạng dạng 3 cột, dưới là bảng lịch sử điểm (ngày, nội dung, ± điểm có màu); (3) Ví voucher — tab Khả dụng/Đã dùng/Hết hạn, grid VoucherCard: mép trái răng cưa, tên voucher, mức giảm nổi bật, điều kiện + hạn ink-500, nút "Dùng ngay"; kèm khu "Đổi điểm lấy voucher" hiện voucher đổi được bằng điểm; (4) Lịch sử — danh sách booking: poster nhỏ, phim, suất, ghế, tổng tiền, BaseBadge trạng thái đúng mục 1.6, bấm mở chi tiết + vé.

### P-09. Đăng nhập / Đăng ký

> [Khối bối cảnh chung] Tạo `pages/LoginPage.vue` và `pages/RegisterPage.vue`: card giữa màn hình max-w-md nền surface-800, logo "Linh Vé Các" chữ brand-500, form BaseInput (đăng ký: họ tên, email, SĐT, ngày sinh — ghi chú "Nhận ưu đãi sinh nhật 🎂", mật khẩu), nút submit primary full-width có loading, link chuyển qua lại đăng nhập/đăng ký, lỗi API hiện ErrorState trên form ("Email hoặc mật khẩu không đúng").

### P-10. Khung admin + dashboard

> LƯU Ý: khu admin dùng LIGHT theme — nền gray-50, card trắng shadow-sm, chữ gray-900/600, màu nhấn vẫn brand-600 (#B8942C), font và quy ước tiền/ngày giữ nguyên như khối bối cảnh chung. Tạo `layouts/AdminLayout.vue`: sidebar tối surface-900 cố định (logo, menu: Dashboard, Phim, Lịch chiếu, Rạp & phòng, Bắp nước, Chiến dịch KM, Voucher, Thành viên; item active nền brand-500/10 viền trái brand-500), header trắng có tên admin + nút đăng xuất. Trang `pages/admin/DashboardPage.vue`: 4 stat card (Doanh thu hôm nay, Vé bán hôm nay, Thành viên mới, Booking chờ thanh toán — số to tabular-nums, so sánh hôm qua ±% màu success/danger), biểu đồ doanh thu 30 ngày (cột), bảng top 5 phim theo doanh thu, donut phân bố hạng thành viên dùng đúng màu tier.

### P-11. Admin CRUD (dùng chung cho Phim / Lịch chiếu / Rạp / Bắp nước / Voucher / Chiến dịch)

> [Light theme admin như P-10] Tạo `components/admin/CrudTable.vue` tái sử dụng: thanh trên gồm ô tìm kiếm + filter select + nút "+ Thêm mới" primary; bảng trắng: header gray-500 text-sm uppercase, hàng hover gray-50, cột cuối là nút Sửa/Xóa ghost; phân trang dưới bảng; xóa mở BaseModal xác nhận danger "Hành động này không thể hoàn tác". Form thêm/sửa mở modal hoặc trang riêng với BaseInput/BaseSelect, nút Lưu primary + Hủy ghost. Áp dụng cho trang Phim (cột: poster nhỏ, tên, thể loại, thời lượng, trạng thái NOW_SHOWING/COMING_SOON/ARCHIVED dạng badge, ngày khởi chiếu) và Lịch chiếu (filter theo rạp + ngày; cột: phim, rạp/phòng, giờ bắt đầu-kết thúc, giá vé, trạng thái; cảnh báo trùng giờ phòng màu danger).

### P-12. Admin quản lý ghế phòng chiếu

> [Light theme admin như P-10] Tạo `pages/admin/RoomSeatsPage.vue`: form sinh lưới ghế (số hàng, số cột) + nút "Sinh sơ đồ"; sơ đồ ghế hiển thị như SeatMap người dùng nhưng ở chế độ chỉnh sửa — click ghế để đổi loại xoay vòng STANDARD → VIP → COUPLE → ẩn ghế (lối đi), có thanh công cụ chọn loại rồi quét nhiều ghế; chú giải màu; nút Lưu sơ đồ primary.

### Quy tắc khi dùng prompt

1. Luôn dán **khối bối cảnh chung** (hoặc ghi chú light-theme admin) vào đầu prompt — không dựa vào việc model "nhớ" quy ước.
2. Sinh xong phải rà lại theo checklist: đủ 3 trạng thái Loading/Empty/Error? Tiền đúng định dạng `₫`? Chữ tiếng Việt tự nhiên? Có tái sử dụng component `ui/` thay vì chế mới? Responsive mobile?
3. Component sinh ra đặt đúng vị trí theo cấu trúc trong PLAN.md; nếu prompt sinh ra kiểu dáng lệch quy ước (shadow trên nền tối, cỡ chữ lạ, màu ngoài palette) thì sửa theo DESIGN.md, không sửa DESIGN.md theo code.
