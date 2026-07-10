package com.linhvecac.booking;

import com.linhvecac.booking.dto.SeatMapResponse;
import com.linhvecac.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Sơ đồ ghế của suất chiếu — cần đăng nhập để phân biệt được ghế MINE. */
@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeSeatController {

    private final BookingService bookingService;

    @GetMapping("/{id}/seats")
    @PreAuthorize("hasRole('USER')")
    public SeatMapResponse seatMap(@PathVariable("id") Long id, @AuthenticationPrincipal User user) {
        return bookingService.getSeatMap(id, user);
    }
}
