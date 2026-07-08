package com.linhvecac.payment;

import com.linhvecac.booking.Booking;
import com.linhvecac.booking.BookingService;
import com.linhvecac.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingService bookingService;

    @InjectMocks
    private PaymentProcessor processor;

    private Payment pending(long amount) {
        Booking booking = new Booking();
        booking.setId(50L);
        booking.setCode("LVCAB12CD34");
        booking.setUser(new User());
        Payment p = new Payment();
        p.setId(1L);
        p.setBooking(booking);
        p.setProvider(PaymentProvider.VNPAY);
        p.setTxnRef("LVCAB12CD34-1");
        p.setAmount(amount);
        p.setStatus(PaymentStatus.PENDING);
        return p;
    }

    private VnpayCallback callback(boolean success, long vnpAmount) {
        return new VnpayCallback("LVCAB12CD34-1", success, vnpAmount,
                "14200001", "NCB", "ATM", null, success ? "00" : "24");
    }

    @Test
    void finalizeVnpay_thanhCong_chuyenSuccessVaMarkPaid() {
        Payment p = pending(480_000);
        when(paymentRepository.findByTxnRef("LVCAB12CD34-1")).thenReturn(Optional.of(p));

        FinalizeResult result = processor.finalizeVnpay(callback(true, 48_000_000));

        assertThat(result.status()).isEqualTo(FinalizeResult.Status.CONFIRMED);
        assertThat(result.bookingCode()).isEqualTo("LVCAB12CD34");
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(bookingService).markPaid("LVCAB12CD34");
    }

    @Test
    void finalizeVnpay_goiLaiKhiDaXuLy_traAlreadyDoneKhongMarkPaid() {
        Payment p = pending(480_000);
        p.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByTxnRef("LVCAB12CD34-1")).thenReturn(Optional.of(p));

        FinalizeResult result = processor.finalizeVnpay(callback(true, 48_000_000));

        assertThat(result.status()).isEqualTo(FinalizeResult.Status.ALREADY_DONE);
        verify(bookingService, never()).markPaid(any());
    }

    @Test
    void finalizeVnpay_saiSoTien_traAmountMismatchVaFailed() {
        Payment p = pending(480_000);
        when(paymentRepository.findByTxnRef("LVCAB12CD34-1")).thenReturn(Optional.of(p));

        FinalizeResult result = processor.finalizeVnpay(callback(true, 99)); // số tiền không khớp

        assertThat(result.status()).isEqualTo(FinalizeResult.Status.AMOUNT_MISMATCH);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(bookingService, never()).markPaid(any());
    }

    @Test
    void finalizeVnpay_khachHuy_traFailedKhongMarkPaid() {
        Payment p = pending(480_000);
        when(paymentRepository.findByTxnRef("LVCAB12CD34-1")).thenReturn(Optional.of(p));

        FinalizeResult result = processor.finalizeVnpay(callback(false, 48_000_000));

        assertThat(result.status()).isEqualTo(FinalizeResult.Status.FAILED);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(bookingService, never()).markPaid(any());
    }

    @Test
    void finalizeMock_thanhCong_chuyenSuccessVaMarkPaid() {
        Payment p = pending(480_000);
        when(paymentRepository.findByTxnRef("LVCAB12CD34-1")).thenReturn(Optional.of(p));

        FinalizeResult result = processor.finalizeMock("LVCAB12CD34-1", true);

        assertThat(result.status()).isEqualTo(FinalizeResult.Status.CONFIRMED);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(bookingService).markPaid("LVCAB12CD34");
    }

    @Test
    void finalizeMock_khongThayTxnRef_traNotFound() {
        when(paymentRepository.findByTxnRef("X")).thenReturn(Optional.empty());

        FinalizeResult result = processor.finalizeMock("X", true);

        assertThat(result.status()).isEqualTo(FinalizeResult.Status.NOT_FOUND);
        verify(bookingService, never()).markPaid(any());
    }
}
