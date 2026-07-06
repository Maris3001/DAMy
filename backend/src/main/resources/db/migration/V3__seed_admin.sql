-- V3: seed tài khoản admin (chỉ dùng cho dev/demo).
-- Đăng nhập: admin@linhvecac.vn / Admin@123456 (hash BCrypt cost 10).
INSERT INTO users (email, password_hash, full_name, role)
VALUES (N'admin@linhvecac.vn',
        N'$2a$10$W7Q1N700G0RuAZQhUQMqk.JytYtbYEryALjHAyjQs.Ufn9Fg5L.6y',
        N'Quản trị viên Linh Vé Các',
        'ADMIN');
