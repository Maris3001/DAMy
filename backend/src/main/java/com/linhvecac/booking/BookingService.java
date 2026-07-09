package com.linhvecac.booking;

import com.linhvecac.booking.dto.BookingResponse;
import com.linhvecac.booking.dto.BookingSummaryResponse;
import com.linhvecac.booking.dto.ConcessionLine;
import com.linhvecac.booking.dto.CreateBookingRequest;
import com.linhvecac.booking.dto.HeldSeat;
import com.linhvecac.booking.dto.HoldRequest;
import com.linhvecac.booking.dto.HoldResponse;
import com.linhvecac.booking.dto.QuoteResponse;
import com.linhvecac.booking.dto.SeatMapResponse;
import com.linhvecac.catalog.cinema.Seat;
import com.linhvecac.catalog.cinema.SeatRepository;
import com.linhvecac.catalog.concession.Concession;
import com.linhvecac.catalog.concession.ConcessionRepository;
import com.linhvecac.catalog.showtime.Showtime;
import com.linhvecac.catalog.showtime.ShowtimeRepository;
import com.linhvecac.catalog.showtime.ShowtimeStatus;
import com.linhvecac.common.ApiException;
import com.linhvecac.loyalty.LoyaltyService;
import com.linhvecac.promotion.VoucherService;
import com.linhvecac.promotion.dto.AppliedVoucher;
import com.linhvecac.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    /** TTL giữ ghế và cửa sổ thanh toán (phút). */
    static final int HOLD_TTL_MIN = 10;
    /** Số ghế tối đa một user được giữ cho một suất. */
    static final int MAX_SEATS = 8;

    private static final String CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingConcessionRepository bookingConcessionRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final ConcessionRepository concessionRepository;
    private final LoyaltyService loyaltyService;
    private final VoucherService voucherService;

    /**
     * Giữ thêm ghế: dọn hold hết hạn của các ghế xin giữ rồi INSERT — UNIQUE(showtime_id, seat_id)
     * ở DB là chốt chặn race; vi phạm unique → 409. Hạn giữ kế thừa hold đang có (1 countdown ổn định).
     */
    @Transactional
    public HoldResponse hold(User user, HoldRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Showtime showtime = getOpenShowtime(request.showtimeId(), now);

        List<Seat> seats = seatRepository.findAllById(request.seatIds());
        if (seats.size() != request.seatIds().size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy ghế");
        }
        for (Seat seat : seats) {
            if (!seat.getRoom().getId().equals(showtime.getRoom().getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Ghế không thuộc phòng chiếu của suất này");
            }
        }

        // Dọn hold hết hạn TRƯỚC khi load hold của user: query @Modifying clear persistence
        // context nên mọi entity phải được nạp sau bước này (tránh LazyInitializationException).
        bookingSeatRepository.deleteExpiredHoldsForSeats(showtime.getId(), request.seatIds(), now);

        List<BookingSeat> myHolds = bookingSeatRepository.findLiveHolds(showtime.getId(), user.getId(), now);
        Set<Long> mySeatIds = myHolds.stream().map(bs -> bs.getSeat().getId()).collect(Collectors.toSet());
        List<Seat> newSeats = seats.stream().filter(s -> !mySeatIds.contains(s.getId())).toList();
        if (myHolds.size() + newSeats.size() > MAX_SEATS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ được chọn tối đa " + MAX_SEATS + " ghế");
        }
        if (newSeats.isEmpty()) {
            return toHoldResponse(myHolds);
        }

        LocalDateTime expiresAt = myHolds.stream()
                .map(BookingSeat::getHoldExpiresAt)
                .min(Comparator.naturalOrder())
                .orElse(now.plusMinutes(HOLD_TTL_MIN));

        List<BookingSeat> inserted = new ArrayList<>();
        for (Seat seat : newSeats) {
            BookingSeat bs = new BookingSeat();
            bs.setShowtime(showtime);
            bs.setSeat(seat);
            bs.setUser(user);
            bs.setStatus(BookingSeatStatus.HOLD);
            bs.setPrice(PricePolicy.seatPrice(showtime.getBasePrice(), seat.getSeatType()));
            bs.setHoldExpiresAt(expiresAt);
            inserted.add(bs);
        }
        try {
            // Flush tường minh để vi phạm unique nổ tại đây (trong service), không nổ lúc commit.
            bookingSeatRepository.saveAllAndFlush(inserted);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT, "Ghế đã có người giữ");
        }

        List<BookingSeat> all = new ArrayList<>(myHolds);
        all.addAll(inserted);
        return toHoldResponse(all);
    }

    /** Nhả ghế user tự bỏ chọn; trả về các ghế còn giữ. */
    @Transactional
    public HoldResponse release(User user, HoldRequest request) {
        bookingSeatRepository.releaseHolds(request.showtimeId(), request.seatIds(), user.getId());
        return toHoldResponse(
                bookingSeatRepository.findLiveHolds(request.showtimeId(), user.getId(), LocalDateTime.now()));
    }

    /** Sơ đồ ghế theo góc nhìn user hiện tại: AVAILABLE / MINE / HELD / BOOKED + giá từng ghế. */
    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long showtimeId, User user) {
        LocalDateTime now = LocalDateTime.now();
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy suất chiếu"));

        List<Seat> seats = seatRepository.findByRoomIdOrderByRowLabelAscColNumberAsc(showtime.getRoom().getId());
        List<BookingSeat> active = bookingSeatRepository.findActiveByShowtime(showtimeId, now);

        Map<Long, BookingSeat> bySeatId = new LinkedHashMap<>();
        for (BookingSeat bs : active) {
            bySeatId.put(bs.getSeat().getId(), bs);
        }

        LocalDateTime holdExpiresAt = null;
        List<SeatMapResponse.SeatItem> items = new ArrayList<>();
        for (Seat seat : seats) {
            BookingSeat bs = bySeatId.get(seat.getId());
            SeatMapResponse.SeatState state = SeatMapResponse.SeatState.AVAILABLE;
            if (bs != null) {
                if (bs.getStatus() == BookingSeatStatus.CONFIRMED) {
                    state = SeatMapResponse.SeatState.BOOKED;
                } else if (bs.getUser().getId().equals(user.getId())) {
                    state = SeatMapResponse.SeatState.MINE;
                    if (bs.getBooking() == null
                            && (holdExpiresAt == null || bs.getHoldExpiresAt().isBefore(holdExpiresAt))) {
                        holdExpiresAt = bs.getHoldExpiresAt();
                    }
                } else {
                    state = SeatMapResponse.SeatState.HELD;
                }
            }
            items.add(new SeatMapResponse.SeatItem(
                    seat.getId(), seat.getRowLabel(), seat.getColNumber(), seat.getSeatType(), state,
                    PricePolicy.seatPrice(showtime.getBasePrice(), seat.getSeatType())));
        }
        return new SeatMapResponse(showtimeId, items, holdExpiresAt);
    }

    /** Báo giá từ ghế đang giữ + bắp nước đã chọn; tự áp voucher lợi nhất (hoặc voucherCode do user chọn). */
    @Transactional(readOnly = true)
    public QuoteResponse quote(User user, Long showtimeId, Map<Long, Integer> concessionQty, String voucherCode) {
        LocalDateTime now = LocalDateTime.now();
        List<BookingSeat> holds = bookingSeatRepository.findLiveHolds(showtimeId, user.getId(), now);
        if (holds.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ghế giữ đã hết hạn, vui lòng chọn lại ghế");
        }

        List<QuoteResponse.ConcessionQuoteLine> lines = buildConcessionLines(concessionQty);
        long subtotal = holds.stream().mapToLong(BookingSeat::getPrice).sum()
                + lines.stream().mapToLong(QuoteResponse.ConcessionQuoteLine::lineTotal).sum();
        LocalDateTime holdExpiresAt = holds.stream()
                .map(BookingSeat::getHoldExpiresAt)
                .min(Comparator.naturalOrder())
                .orElse(null);

        AppliedVoucher applied = voucherService.resolveForQuote(user, subtotal, voucherCode);
        long discount = applied != null ? applied.discount() : 0;

        return new QuoteResponse(
                holds.stream().map(HeldSeat::from).toList(),
                lines, subtotal, discount, subtotal - discount,
                applied != null ? applied.code() : null,
                applied != null ? applied.name() : null,
                holdExpiresAt);
    }

    /**
     * Tạo đơn PENDING_PAYMENT từ ghế đang giữ của user: chốt giá, gắn booking vào hold và
     * đồng bộ hạn giữ = hạn đơn (job dọn đơn quá hạn đồng thời nhả được ghế).
     */
    @Transactional
    public BookingResponse create(User user, CreateBookingRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Showtime showtime = getOpenShowtime(request.showtimeId(), now);

        List<BookingSeat> holds = bookingSeatRepository.findLiveHolds(showtime.getId(), user.getId(), now);
        if (holds.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ghế giữ đã hết hạn, vui lòng chọn lại ghế");
        }

        List<QuoteResponse.ConcessionQuoteLine> lines =
                buildConcessionLines(toConcessionQty(request.concessions()));
        long subtotal = holds.stream().mapToLong(BookingSeat::getPrice).sum()
                + lines.stream().mapToLong(QuoteResponse.ConcessionQuoteLine::lineTotal).sum();

        // Chốt voucher server-side (không tin discount từ client): resolve rồi giữ chỗ phiếu.
        AppliedVoucher applied = voucherService.resolveForQuote(user, subtotal, request.voucherCode());
        long discount = applied != null ? applied.discount() : 0;

        Booking booking = new Booking();
        booking.setCode(generateCode());
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setSubtotal(subtotal);
        booking.setDiscount(discount);
        booking.setTotal(subtotal - discount);
        booking.setExpiresAt(now.plusMinutes(HOLD_TTL_MIN));
        bookingRepository.save(booking);

        if (applied != null) {
            // Guard-update: nếu phiếu vừa bị đơn khác chiếm giữa chừng → 409, đơn này chưa gắn hold nên rollback sạch.
            voucherService.reserve(user, applied.code(), booking.getId());
        }

        for (BookingSeat hold : holds) {
            hold.setBooking(booking);
            hold.setHoldExpiresAt(booking.getExpiresAt());
        }
        bookingSeatRepository.saveAll(holds);

        List<BookingConcession> items = new ArrayList<>();
        for (QuoteResponse.ConcessionQuoteLine line : lines) {
            BookingConcession item = new BookingConcession();
            item.setBooking(booking);
            item.setConcession(concessionRepository.getReferenceById(line.concessionId()));
            item.setQuantity(line.quantity());
            item.setUnitPrice(line.unitPrice());
            items.add(item);
        }
        bookingConcessionRepository.saveAll(items);

        return BookingResponse.from(booking, holds, items,
                applied != null ? applied.code() : null,
                applied != null ? applied.name() : null);
    }

    /**
     * Chốt đơn đã thanh toán (gọi từ payment callback P5) — idempotent nhờ guard-update:
     * PENDING_PAYMENT → PAID, ghế HOLD → CONFIRMED + sinh ticket_code (kiêm vé).
     * Trả false khi đơn đã PAID (callback gọi lại) hoặc đã EXPIRED — không xử lý gì thêm.
     */
    @Transactional
    public boolean markPaid(String code) {
        // Guard-update là @Modifying(clearAutomatically) → chạy TRƯỚC mọi lần load entity.
        if (bookingRepository.markPaidIfPending(code) == 0) {
            return false;
        }
        Booking booking = bookingRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn"));
        List<BookingSeat> seats = bookingSeatRepository.findByBookingId(booking.getId());
        for (BookingSeat seat : seats) {
            seat.setStatus(BookingSeatStatus.CONFIRMED);
            seat.setHoldExpiresAt(null);
            seat.setTicketCode(generateTicketCode());
        }
        bookingSeatRepository.saveAll(seats);
        // Tích điểm + xét hạng trong cùng transaction — guard-update ở trên đảm bảo chỉ chạy 1 lần/đơn.
        // Điểm tính trên booking.total (đã trừ voucher) — đúng ý: tích trên số tiền thực trả.
        loyaltyService.awardForBooking(booking.getUser(), booking.getId(), booking.getTotal());
        // Voucher đã giữ cho đơn (nếu có) → chuyển USED.
        voucherService.markUsedForBooking(booking.getId());
        return true;
    }

    /** Danh sách đơn của user (mới nhất trước) cho trang "Vé của tôi". */
    @Transactional(readOnly = true)
    public List<BookingSummaryResponse> listMine(User user) {
        return bookingRepository.findMineWithDetails(user.getId()).stream()
                .map(b -> BookingSummaryResponse.from(b, (int) bookingSeatRepository.countByBookingId(b.getId())))
                .toList();
    }

    /** Chi tiết đơn theo mã — owner-guard: đơn của người khác trả 404 (không lộ mã đơn tồn tại). */
    @Transactional(readOnly = true)
    public BookingResponse getByCode(User user, String code) {
        Booking booking = bookingRepository.findByCode(code)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn"));
        AppliedVoucher voucher = voucherService.describeForBooking(booking.getId());
        return BookingResponse.from(booking,
                bookingSeatRepository.findByBookingId(booking.getId()),
                bookingConcessionRepository.findByBookingId(booking.getId()),
                voucher != null ? voucher.code() : null,
                voucher != null ? voucher.name() : null);
    }

    /** Job 60s: đơn quá hạn → EXPIRED trước, rồi xóa hold hết hạn (nhả ghế, kể cả ghế của đơn vừa hết hạn). */
    @Transactional
    public void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        bookingRepository.expirePendingBefore(now);
        bookingSeatRepository.deleteExpiredHolds(now);
        // Nhả voucher của đơn vừa EXPIRED về AVAILABLE + đánh dấu phiếu quá hạn dùng.
        voucherService.cleanupExpired(now);
    }

    private Showtime getOpenShowtime(Long showtimeId, LocalDateTime now) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy suất chiếu"));
        if (showtime.getStatus() != ShowtimeStatus.OPEN || !showtime.getStartsAt().isAfter(now)) {
            throw new ApiException(HttpStatus.CONFLICT, "Suất chiếu đã đóng hoặc đã bắt đầu");
        }
        return showtime;
    }

    private List<QuoteResponse.ConcessionQuoteLine> buildConcessionLines(Map<Long, Integer> concessionQty) {
        List<QuoteResponse.ConcessionQuoteLine> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : concessionQty.entrySet()) {
            int quantity = entry.getValue();
            if (quantity < 1 || quantity > 10) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Số lượng mỗi món phải từ 1 đến 10");
            }
            Concession concession = concessionRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy món bắp nước"));
            if (!concession.isActive()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Món " + concession.getName() + " đã ngừng bán");
            }
            lines.add(new QuoteResponse.ConcessionQuoteLine(
                    concession.getId(), concession.getName(), quantity,
                    concession.getPrice(), concession.getPrice() * quantity));
        }
        return lines;
    }

    private Map<Long, Integer> toConcessionQty(List<ConcessionLine> concessions) {
        Map<Long, Integer> qty = new LinkedHashMap<>();
        if (concessions != null) {
            for (ConcessionLine line : concessions) {
                qty.merge(line.concessionId(), line.quantity(), Integer::sum);
            }
        }
        return qty;
    }

    private HoldResponse toHoldResponse(List<BookingSeat> holds) {
        LocalDateTime expiresAt = holds.stream()
                .map(BookingSeat::getHoldExpiresAt)
                .min(Comparator.naturalOrder())
                .orElse(null);
        return new HoldResponse(holds.stream().map(HeldSeat::from).toList(), expiresAt);
    }

    private String generateCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("LVC");
            for (int i = 0; i < 8; i++) {
                sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (bookingRepository.existsByCode(code));
        return code;
    }

    /** Mã vé "VE" + 8 ký tự — filtered unique index ux_booking_seats_ticket là chốt cuối ở DB. */
    private String generateTicketCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("VE");
            for (int i = 0; i < 8; i++) {
                sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (bookingSeatRepository.existsByTicketCode(code));
        return code;
    }
}
