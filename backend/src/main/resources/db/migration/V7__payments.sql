-- V7: payments P5 — giao dịch thanh toán (VNPay sandbox / mock), mỗi lượt thử = 1 row.
-- txn_ref = booking.code + '-' + attempt (VNPay dedup theo vnp_TxnRef nên retry cần ref mới).

CREATE TABLE payments (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id     BIGINT        NOT NULL,
    provider       NVARCHAR(20)  NOT NULL,
    txn_ref        NVARCHAR(30)  NOT NULL,
    amount         BIGINT        NOT NULL,
    status         NVARCHAR(20)  NOT NULL CONSTRAINT df_payments_status DEFAULT 'PENDING',
    response_code  NVARCHAR(10)  NULL,
    transaction_no NVARCHAR(20)  NULL,
    bank_code      NVARCHAR(20)  NULL,
    card_type      NVARCHAR(20)  NULL,
    paid_at        DATETIME2     NULL,
    created_at     DATETIME2     NOT NULL CONSTRAINT df_payments_created DEFAULT SYSDATETIME(),
    updated_at     DATETIME2     NULL,
    CONSTRAINT uq_payments_txn_ref UNIQUE (txn_ref),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT ck_payments_provider CHECK (provider IN ('VNPAY', 'MOCK')),
    CONSTRAINT ck_payments_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'))
);
CREATE INDEX ix_payments_booking ON payments(booking_id);
