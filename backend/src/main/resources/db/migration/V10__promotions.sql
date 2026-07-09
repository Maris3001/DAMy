-- V10: promotions P7 — voucher, chiến dịch KM, ưu đãi cá nhân hóa.
-- Mô hình 2 bảng: campaigns (định nghĩa quy tắc ưu đãi) + user_vouchers (phiếu người dùng sở hữu).
-- users.birth_date đã có sẵn từ V2 (nhận ưu đãi sinh nhật).

-- Định nghĩa quy tắc ưu đãi. voucher_type PERCENT dùng discount_value là % (1-100) + cap max_discount_amount;
-- FIXED dùng discount_value là số tiền VND. points_cost ≠ NULL → campaign cho phép đổi điểm (trigger REDEEM).
CREATE TABLE campaigns (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    code                NVARCHAR(40)  NOT NULL,
    name                NVARCHAR(150) NOT NULL,
    description         NVARCHAR(500) NULL,
    voucher_type        NVARCHAR(20)  NOT NULL,
    discount_value      BIGINT        NOT NULL,
    max_discount_amount BIGINT        NULL,
    min_order_amount    BIGINT        NOT NULL CONSTRAINT df_campaigns_min_order DEFAULT 0,
    min_tier            NVARCHAR(20)  NULL,
    points_cost         INT           NULL,
    trigger_type        NVARCHAR(20)  NOT NULL,
    valid_days          INT           NOT NULL CONSTRAINT df_campaigns_valid_days DEFAULT 30,
    quantity            INT           NULL,
    per_user_limit      INT           NOT NULL CONSTRAINT df_campaigns_per_user DEFAULT 1,
    is_active           BIT           NOT NULL CONSTRAINT df_campaigns_active DEFAULT 1,
    created_at          DATETIME2     NOT NULL CONSTRAINT df_campaigns_created DEFAULT SYSDATETIME(),
    CONSTRAINT uq_campaigns_code UNIQUE (code),
    CONSTRAINT ck_campaigns_type CHECK (voucher_type IN ('PERCENT', 'FIXED')),
    CONSTRAINT ck_campaigns_min_tier CHECK (min_tier IN ('SILVER', 'GOLD', 'PLATINUM')),
    CONSTRAINT ck_campaigns_trigger CHECK (
        trigger_type IN ('MANUAL', 'REDEEM', 'BIRTHDAY', 'WINBACK', 'GENRE_FAVORITE', 'TIER_UP'))
);

-- Phiếu người dùng sở hữu. Vòng đời status: AVAILABLE → RESERVED (gắn booking) → USED, hoặc → EXPIRED.
-- user_id/booking_id để dạng BIGINT thuần (không FK entity ở tầng JPA) — promotion không phụ thuộc ngược booking.
CREATE TABLE user_vouchers (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    code         NVARCHAR(20)  NOT NULL,
    campaign_id  BIGINT        NOT NULL,
    user_id      BIGINT        NOT NULL,
    status       NVARCHAR(20)  NOT NULL CONSTRAINT df_user_vouchers_status DEFAULT 'AVAILABLE',
    valid_from   DATETIME2     NOT NULL,
    valid_to     DATETIME2     NOT NULL,
    booking_id   BIGINT        NULL,
    period_key   NVARCHAR(20)  NULL,
    reserved_at  DATETIME2     NULL,
    used_at      DATETIME2     NULL,
    created_at   DATETIME2     NOT NULL CONSTRAINT df_user_vouchers_created DEFAULT SYSDATETIME(),
    CONSTRAINT uq_user_vouchers_code UNIQUE (code),
    CONSTRAINT fk_user_vouchers_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
    CONSTRAINT fk_user_vouchers_user     FOREIGN KEY (user_id)     REFERENCES users(id),
    CONSTRAINT fk_user_vouchers_booking  FOREIGN KEY (booking_id)  REFERENCES bookings(id),
    CONSTRAINT ck_user_vouchers_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'USED', 'EXPIRED'))
);
-- Chốt idempotency cho OfferEngine ở tầng DB: mỗi (campaign, user, kỳ) chỉ cấp 1 lần. Dùng filtered unique
-- index (bỏ qua period_key NULL) để voucher REDEEM/MANUAL — vốn không đặt period_key — không bị chặn.
CREATE UNIQUE INDEX ux_user_vouchers_grant
    ON user_vouchers(campaign_id, user_id, period_key) WHERE period_key IS NOT NULL;
CREATE INDEX ix_user_vouchers_user_status ON user_vouchers(user_id, status);
CREATE INDEX ix_user_vouchers_booking ON user_vouchers(booking_id);
