package com.linhvecac.booking;

import com.linhvecac.booking.dto.BookingResponse;
import com.linhvecac.booking.dto.BookingSummaryResponse;
import com.linhvecac.booking.dto.CreateBookingRequest;
import com.linhvecac.booking.dto.HoldRequest;
import com.linhvecac.booking.dto.HoldResponse;
import com.linhvecac.booking.dto.QuoteResponse;
import com.linhvecac.common.ApiException;
import com.linhvecac.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // Chỉ thành viên (USER) được thực hiện luồng mua vé; tài khoản ADMIN quản trị ở khu riêng.
    @PostMapping("/hold")
    @PreAuthorize("hasRole('USER')")
    public HoldResponse hold(@AuthenticationPrincipal User user, @Valid @RequestBody HoldRequest request) {
        return bookingService.hold(user, request);
    }

    @DeleteMapping("/hold")
    @PreAuthorize("hasRole('USER')")
    public HoldResponse release(@AuthenticationPrincipal User user, @Valid @RequestBody HoldRequest request) {
        return bookingService.release(user, request);
    }

    /** Báo giá trước khi tạo đơn; concessions dạng "id:soLuong,id:soLuong"; voucher = mã phiếu user chọn (tùy chọn). */
    @GetMapping("/quote")
    @PreAuthorize("hasRole('USER')")
    public QuoteResponse quote(@AuthenticationPrincipal User user,
                               @RequestParam(name = "showtimeId") Long showtimeId,
                               @RequestParam(name = "concessions", required = false) String concessions,
                               @RequestParam(name = "voucher", required = false) String voucher) {
        return bookingService.quote(user, showtimeId, parseConcessions(concessions), voucher);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public BookingResponse create(@AuthenticationPrincipal User user,
                                  @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(user, request);
    }

    /** Danh sách đơn của user hiện tại (mới nhất trước) — trang "Vé của tôi". */
    @GetMapping
    public List<BookingSummaryResponse> listMine(@AuthenticationPrincipal User user) {
        return bookingService.listMine(user);
    }

    /** Chi tiết đơn (kèm vé nếu đã thanh toán) — chỉ chủ đơn xem được, khác chủ trả 404. */
    @GetMapping("/{code}")
    public BookingResponse getByCode(@AuthenticationPrincipal User user, @PathVariable("code") String code) {
        return bookingService.getByCode(user, code);
    }

    private Map<Long, Integer> parseConcessions(String raw) {
        Map<Long, Integer> qty = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return qty;
        }
        try {
            for (String pair : raw.split(",")) {
                String[] parts = pair.split(":");
                qty.merge(Long.parseLong(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer::sum);
            }
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Danh sách bắp nước không hợp lệ");
        }
        return qty;
    }
}
