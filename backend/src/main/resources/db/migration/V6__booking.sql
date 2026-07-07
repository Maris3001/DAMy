-- V6: booking P4 — đơn đặt vé, ghế giữ/đã bán (kiêm vé), bắp nước theo đơn.
-- booking_seats.uq_booking_seats_showtime_seat là chốt chặn race condition khi 2 người tranh 1 ghế.

CREATE TABLE bookings (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    code        NVARCHAR(12)  NOT NULL,
    user_id     BIGINT        NOT NULL,
    showtime_id BIGINT        NOT NULL,
    status      NVARCHAR(20)  NOT NULL CONSTRAINT df_bookings_status DEFAULT 'PENDING_PAYMENT',
    subtotal    BIGINT        NOT NULL,
    discount    BIGINT        NOT NULL CONSTRAINT df_bookings_discount DEFAULT 0,
    total       BIGINT        NOT NULL,
    expires_at  DATETIME2     NOT NULL,
    created_at  DATETIME2     NOT NULL CONSTRAINT df_bookings_created DEFAULT SYSDATETIME(),
    CONSTRAINT uq_bookings_code UNIQUE (code),
    CONSTRAINT fk_bookings_user     FOREIGN KEY (user_id)     REFERENCES users(id),
    CONSTRAINT fk_bookings_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(id),
    CONSTRAINT ck_bookings_status CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'EXPIRED', 'CANCELLED'))
);
CREATE INDEX ix_bookings_status_expires ON bookings(status, expires_at);
CREATE INDEX ix_bookings_user ON bookings(user_id);

-- Ghế giữ tạm (HOLD, booking_id NULL khi chưa tạo đơn) và ghế đã bán (CONFIRMED, kiêm luôn vé).
-- Nhả ghế = DELETE row (UNIQUE không filter nên không dùng status flip).
CREATE TABLE booking_seats (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    showtime_id     BIGINT        NOT NULL,
    seat_id         BIGINT        NOT NULL,
    user_id         BIGINT        NOT NULL,
    booking_id      BIGINT        NULL,
    status          NVARCHAR(20)  NOT NULL CONSTRAINT df_booking_seats_status DEFAULT 'HOLD',
    price           BIGINT        NOT NULL,
    hold_expires_at DATETIME2     NULL,
    ticket_code     NVARCHAR(20)  NULL,
    created_at      DATETIME2     NOT NULL CONSTRAINT df_booking_seats_created DEFAULT SYSDATETIME(),
    CONSTRAINT uq_booking_seats_showtime_seat UNIQUE (showtime_id, seat_id),
    CONSTRAINT fk_booking_seats_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(id),
    CONSTRAINT fk_booking_seats_seat     FOREIGN KEY (seat_id)     REFERENCES seats(id),
    CONSTRAINT fk_booking_seats_user     FOREIGN KEY (user_id)     REFERENCES users(id),
    CONSTRAINT fk_booking_seats_booking  FOREIGN KEY (booking_id)  REFERENCES bookings(id),
    CONSTRAINT ck_booking_seats_status CHECK (status IN ('HOLD', 'CONFIRMED'))
);
-- UNIQUE thường của SQL Server chỉ cho 1 NULL → dùng filtered unique index cho mã vé.
CREATE UNIQUE INDEX ux_booking_seats_ticket ON booking_seats(ticket_code) WHERE ticket_code IS NOT NULL;
CREATE INDEX ix_booking_seats_status_expires ON booking_seats(status, hold_expires_at);
CREATE INDEX ix_booking_seats_booking ON booking_seats(booking_id);

CREATE TABLE booking_concessions (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id    BIGINT NOT NULL,
    concession_id BIGINT NOT NULL,
    quantity      INT    NOT NULL,
    unit_price    BIGINT NOT NULL,
    CONSTRAINT fk_booking_concessions_booking    FOREIGN KEY (booking_id)    REFERENCES bookings(id),
    CONSTRAINT fk_booking_concessions_concession FOREIGN KEY (concession_id) REFERENCES concessions(id),
    CONSTRAINT ck_booking_concessions_qty CHECK (quantity BETWEEN 1 AND 10)
);
CREATE INDEX ix_booking_concessions_booking ON booking_concessions(booking_id);
