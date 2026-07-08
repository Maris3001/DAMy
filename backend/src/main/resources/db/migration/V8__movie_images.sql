-- V8: gán poster + ảnh nền (backdrop) cho phim seed. File SVG tĩnh nằm ở frontend/public,
-- phục vụ same-origin qua /posters/** và /backdrops/** (Vite dev + bản build).

UPDATE movies SET poster_url = N'/posters/vu-tru-song-song.svg',    backdrop_url = N'/backdrops/vu-tru-song-song.svg'    WHERE title = N'Vũ Trụ Song Song';
UPDATE movies SET poster_url = N'/posters/bi-mat-trong-suong.svg',  backdrop_url = N'/backdrops/bi-mat-trong-suong.svg'  WHERE title = N'Bí Mật Trong Sương';
UPDATE movies SET poster_url = N'/posters/nha-co-nam-nang-tien.svg', backdrop_url = N'/backdrops/nha-co-nam-nang-tien.svg' WHERE title = N'Nhà Có Năm Nàng Tiên';
UPDATE movies SET poster_url = N'/posters/thanh-pho-khong-ngu.svg', backdrop_url = N'/backdrops/thanh-pho-khong-ngu.svg' WHERE title = N'Thành Phố Không Ngủ';
UPDATE movies SET poster_url = N'/posters/chuyen-tau-cuoi-cung.svg', backdrop_url = N'/backdrops/chuyen-tau-cuoi-cung.svg' WHERE title = N'Chuyến Tàu Cuối Cùng';
UPDATE movies SET poster_url = N'/posters/rung-xanh-ky-bi.svg',    backdrop_url = N'/backdrops/rung-xanh-ky-bi.svg'    WHERE title = N'Rừng Xanh Kỳ Bí';
