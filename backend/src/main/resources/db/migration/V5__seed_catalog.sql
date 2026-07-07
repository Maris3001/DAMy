-- V5: seed catalog để demo P3 và làm dữ liệu nền cho P4 (đặt vé).
-- Ghế sinh set-based cho mọi phòng (8 hàng A-H × 10 cột). Suất chiếu neo theo NGÀY HIỆN TẠI
-- (CAST(SYSDATETIME() AS DATE)) để trang public luôn có suất trong 7 ngày tới khi demo.

-- ===== Khu vực =====
DECLARE @regHN BIGINT, @regHCM BIGINT;
INSERT INTO regions (name, sort_order) VALUES (N'Hà Nội', 1);
SET @regHN = SCOPE_IDENTITY();
INSERT INTO regions (name, sort_order) VALUES (N'TP. Hồ Chí Minh', 2);
SET @regHCM = SCOPE_IDENTITY();

-- ===== Hãng rạp =====
DECLARE @chainCGV BIGINT, @chainGLX BIGINT;
INSERT INTO cinema_chains (name) VALUES (N'CGV Cinemas');
SET @chainCGV = SCOPE_IDENTITY();
INSERT INTO cinema_chains (name) VALUES (N'Galaxy Cinema');
SET @chainGLX = SCOPE_IDENTITY();

-- ===== Rạp =====
DECLARE @cin1 BIGINT, @cin2 BIGINT, @cin3 BIGINT;
INSERT INTO cinemas (chain_id, region_id, name, address)
VALUES (@chainCGV, @regHN, N'CGV Vincom Bà Triệu', N'191 Bà Triệu, Hai Bà Trưng, Hà Nội');
SET @cin1 = SCOPE_IDENTITY();
INSERT INTO cinemas (chain_id, region_id, name, address)
VALUES (@chainGLX, @regHN, N'Galaxy Mipec Long Biên', N'2 Long Biên 2, Ngọc Lâm, Hà Nội');
SET @cin2 = SCOPE_IDENTITY();
INSERT INTO cinemas (chain_id, region_id, name, address)
VALUES (@chainCGV, @regHCM, N'CGV Vincom Đồng Khởi', N'72 Lê Thánh Tôn, Quận 1, TP. HCM');
SET @cin3 = SCOPE_IDENTITY();

-- ===== Phòng chiếu (mỗi rạp 2 phòng) =====
INSERT INTO rooms (cinema_id, name, room_type) VALUES
    (@cin1, N'Phòng 1', '2D'), (@cin1, N'Phòng 2', '3D'),
    (@cin2, N'Phòng 1', '2D'), (@cin2, N'Phòng 2', '2D'),
    (@cin3, N'Phòng 1', 'IMAX'), (@cin3, N'Phòng 2', '2D');

-- ===== Ghế: 8 hàng A-H × 10 cột cho MỌI phòng (F-G là VIP, H là ghế đôi) =====
WITH rows_cte AS (
    SELECT row_label, seat_type FROM (VALUES
        (N'A', 'STANDARD'), (N'B', 'STANDARD'), (N'C', 'STANDARD'),
        (N'D', 'STANDARD'), (N'E', 'STANDARD'),
        (N'F', 'VIP'), (N'G', 'VIP'), (N'H', 'COUPLE')
    ) r(row_label, seat_type)),
cols_cte AS (
    SELECT n FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) c(n))
INSERT INTO seats (room_id, row_label, col_number, seat_type)
SELECT rm.id, r.row_label, c.n, r.seat_type
FROM rooms rm CROSS JOIN rows_cte r CROSS JOIN cols_cte c;

-- ===== Thể loại =====
INSERT INTO genres (name) VALUES
    (N'Khoa học viễn tưởng'), (N'Kinh dị'), (N'Bí ẩn'), (N'Hài'),
    (N'Gia đình'), (N'Hành động'), (N'Chính kịch'), (N'Hoạt hình');

-- ===== Phim =====
INSERT INTO movies (title, description, duration_min, age_rating, status, release_date) VALUES
    (N'Vũ Trụ Song Song', N'Một nhà vật lý trẻ vô tình mở ra cánh cửa tới các vũ trụ song song và phải tìm đường trở về.', 128, 'T13', 'NOW_SHOWING', '2026-06-20'),
    (N'Bí Mật Trong Sương', N'Ngôi làng nhỏ chìm trong màn sương dày cùng những vụ mất tích không lời giải.', 105, 'T16', 'NOW_SHOWING', '2026-06-27'),
    (N'Nhà Có Năm Nàng Tiên', N'Câu chuyện hài hước, ấm áp về gia đình có năm cô con gái cá tính.', 112, 'P', 'NOW_SHOWING', '2026-07-01'),
    (N'Thành Phố Không Ngủ', N'Một thám tử lần theo dấu vết tội phạm xuyên màn đêm đô thị.', 96, 'T18', 'NOW_SHOWING', '2026-07-04'),
    (N'Chuyến Tàu Cuối Cùng', N'Những mảnh đời gặp nhau trên chuyến tàu định mệnh cuối năm.', 134, 'T13', 'COMING_SOON', '2026-07-25'),
    (N'Rừng Xanh Kỳ Bí', N'Hành trình phiêu lưu của cô bé và những người bạn thú rừng.', 89, 'P', 'COMING_SOON', '2026-08-01');

-- ===== Gán thể loại cho phim (set-based nối theo tên) =====
INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM (VALUES
    (N'Vũ Trụ Song Song', N'Khoa học viễn tưởng'),
    (N'Bí Mật Trong Sương', N'Kinh dị'), (N'Bí Mật Trong Sương', N'Bí ẩn'),
    (N'Nhà Có Năm Nàng Tiên', N'Hài'), (N'Nhà Có Năm Nàng Tiên', N'Gia đình'),
    (N'Thành Phố Không Ngủ', N'Hành động'),
    (N'Chuyến Tàu Cuối Cùng', N'Chính kịch'),
    (N'Rừng Xanh Kỳ Bí', N'Hoạt hình')
) AS mg(movie_title, genre_name)
JOIN movies m ON m.title = mg.movie_title
JOIN genres g ON g.name = mg.genre_name;

-- ===== Suất chiếu: mỗi phim NOW_SHOWING nhận 1 phòng riêng, chiếu 3 suất/ngày × 7 ngày =====
DECLARE @base DATE = CAST(SYSDATETIME() AS DATE);
WITH nm AS (
        SELECT m.id AS movie_id, m.duration_min,
               ROW_NUMBER() OVER (ORDER BY m.id) AS rn
        FROM movies m WHERE m.status = 'NOW_SHOWING'),
     rm AS (
        SELECT r.id AS room_id, ROW_NUMBER() OVER (ORDER BY r.id) AS rn
        FROM rooms r),
     days AS (SELECT n FROM (VALUES (0),(1),(2),(3),(4),(5),(6)) d(n)),
     times AS (SELECT h FROM (VALUES (10),(14),(19)) t(h))
INSERT INTO showtimes (movie_id, room_id, starts_at, ends_at, base_price, status)
SELECT nm.movie_id,
       rm.room_id,
       DATEADD(HOUR, t.h, DATEADD(DAY, d.n, CAST(@base AS DATETIME2))),
       DATEADD(MINUTE, nm.duration_min + 15, DATEADD(HOUR, t.h, DATEADD(DAY, d.n, CAST(@base AS DATETIME2)))),
       90000,
       'OPEN'
FROM nm
JOIN rm ON rm.rn = nm.rn
CROSS JOIN days d
CROSS JOIN times t;

-- ===== Bắp nước =====
INSERT INTO concessions (name, description, price, category) VALUES
    (N'Combo Đôi', N'2 nước ngọt lớn + 1 bắp lớn', 109000, 'COMBO'),
    (N'Combo Solo', N'1 nước ngọt lớn + 1 bắp vừa', 79000, 'COMBO'),
    (N'Combo Gia Đình', N'4 nước ngọt + 2 bắp lớn', 189000, 'COMBO'),
    (N'Bắp rang bơ (lớn)', N'Bắp rang bơ size lớn', 55000, 'SINGLE'),
    (N'Coca-Cola (lớn)', N'Nước ngọt có ga size lớn', 35000, 'SINGLE'),
    (N'Nước suối', N'Chai nước suối 500ml', 20000, 'SINGLE');
