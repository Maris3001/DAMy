package com.linhvecac.booking;

import com.linhvecac.booking.dto.BookingResponse;
import com.linhvecac.booking.dto.CreateBookingRequest;
import com.linhvecac.booking.dto.HoldRequest;
import com.linhvecac.booking.dto.HoldResponse;
import com.linhvecac.booking.dto.QuoteResponse;
import com.linhvecac.common.ApiException;
import com.linhvecac.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/hold")
    public HoldResponse hold(@AuthenticationPrincipal User user, @Valid @RequestBody HoldRequest request) {
        return bookingService.hold(user, request);
    }

    @DeleteMapping("/hold")
    public HoldResponse release(@AuthenticationPrincipal User user, @Valid @RequestBody HoldRequest request) {
        return bookingService.release(user, request);
    }

    /** Báo giá trước khi tạo đơn; concessions dạng "id:soLuong,id:soLuong". */
    @GetMapping("/quote")
    public QuoteResponse quote(@AuthenticationPrincipal User user,
                               @RequestParam Long showtimeId,
                               @RequestParam(required = false) String concessions) {
        return bookingService.quote(user, showtimeId, parseConcessions(concessions));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@AuthenticationPrincipal User user,
                                  @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(user, request);
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
