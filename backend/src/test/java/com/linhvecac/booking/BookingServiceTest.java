package com.linhvecac.booking;

import com.linhvecac.booking.dto.BookingResponse;
import com.linhvecac.booking.dto.ConcessionLine;
import com.linhvecac.booking.dto.CreateBookingRequest;
import com.linhvecac.booking.dto.HoldRequest;
import com.linhvecac.booking.dto.HoldResponse;
import com.linhvecac.catalog.cinema.Cinema;
import com.linhvecac.catalog.cinema.Room;
import com.linhvecac.catalog.cinema.Seat;
import com.linhvecac.catalog.cinema.SeatRepository;
import com.linhvecac.catalog.cinema.SeatType;
import com.linhvecac.catalog.concession.Concession;
import com.linhvecac.catalog.concession.ConcessionRepository;
import com.linhvecac.catalog.movie.Movie;
import com.linhvecac.catalog.showtime.Showtime;
import com.linhvecac.catalog.showtime.ShowtimeRepository;
import com.linhvecac.catalog.showtime.ShowtimeStatus;
import com.linhvecac.common.ApiException;
import com.linhvecac.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingSeatRepository bookingSeatRepository;
    @Mock
    private BookingConcessionRepository bookingConcessionRepository;
    @Mock
    private ShowtimeRepository showtimeRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private ConcessionRepository concessionRepository;

    @InjectMocks
    private BookingService bookingService;

    private User user() {
        User u = new User();
        u.setId(10L);
        u.setEmail("test@linhvecac.vn");
        u.setFullName("Người test");
        return u;
    }

    private Showtime showtimeOpen() {
        Cinema c = new Cinema();
        c.setId(9L);
        c.setName("CGV Test");
        Room r = new Room();
        r.setId(5L);
        r.setName("Phòng 1");
        r.setRoomType("2D");
        r.setCinema(c);
        Movie m = new Movie();
        m.setId(1L);
        m.setTitle("Phim test");
        m.setDurationMin(120);
        Showtime s = new Showtime();
        s.setId(100L);
        s.setMovie(m);
        s.setRoom(r);
        s.setStartsAt(LocalDateTime.now().plusDays(1));
        s.setEndsAt(LocalDateTime.now().plusDays(1).plusMinutes(135));
        s.setBasePrice(90_000);
        s.setStatus(ShowtimeStatus.OPEN);
        return s;
    }

    private Seat seat(long id, String row, int col, SeatType type, Room room) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setRowLabel(row);
        seat.setColNumber(col);
        seat.setSeatType(type);
        seat.setRoom(room);
        return seat;
    }

    private BookingSeat holdOf(User u, Showtime s, Seat seat, LocalDateTime expiresAt) {
        BookingSeat bs = new BookingSeat();
        bs.setShowtime(s);
        bs.setSeat(seat);
        bs.setUser(u);
        bs.setStatus(BookingSeatStatus.HOLD);
        bs.setPrice(PricePolicy.seatPrice(s.getBasePrice(), seat.getSeatType()));
        bs.setHoldExpiresAt(expiresAt);
        return bs;
    }

    @Test
    void hold_khiGheDaCoNguoiGiu_nem409() {
        Showtime s = showtimeOpen();
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(s));
        when(seatRepository.findAllById(List.of(1L))).thenReturn(List.of(seat(1L, "A", 1, SeatType.STANDARD, s.getRoom())));
        when(bookingSeatRepository.findLiveHolds(anyLong(), anyLong(), any())).thenReturn(List.of());
        when(bookingSeatRepository.saveAllAndFlush(anyList()))
                .thenThrow(new DataIntegrityViolationException("uq_booking_seats_showtime_seat"));

        assertThatThrownBy(() -> bookingService.hold(user(), new HoldRequest(100L, List.of(1L))))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getMessage()).isEqualTo("Ghế đã có người giữ");
                });
    }

    @Test
    void hold_khiVuotQua8Ghe_nem400() {
        Showtime s = showtimeOpen();
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(s));
        when(seatRepository.findAllById(List.of(9L))).thenReturn(List.of(seat(9L, "B", 1, SeatType.STANDARD, s.getRoom())));
        // Đã giữ sẵn 8 ghế khác
        List<BookingSeat> eight = new java.util.ArrayList<>();
        for (long i = 1; i <= 8; i++) {
            eight.add(holdOf(user(), s, seat(i, "A", (int) i, SeatType.STANDARD, s.getRoom()),
                    LocalDateTime.now().plusMinutes(9)));
        }
        when(bookingSeatRepository.findLiveHolds(anyLong(), anyLong(), any())).thenReturn(eight);

        assertThatThrownBy(() -> bookingService.hold(user(), new HoldRequest(100L, List.of(9L))))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(bookingSeatRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void hold_khiSuatDaDong_nem409() {
        Showtime s = showtimeOpen();
        s.setStatus(ShowtimeStatus.CLOSED);
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(s));

        assertThatThrownBy(() -> bookingService.hold(user(), new HoldRequest(100L, List.of(1L))))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void hold_khiHopLe_luuGiaTheoLoaiGheVaTtl10Phut() {
        Showtime s = showtimeOpen();
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(s));
        when(seatRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(
                seat(1L, "A", 1, SeatType.STANDARD, s.getRoom()),
                seat(2L, "F", 1, SeatType.VIP, s.getRoom())));
        when(bookingSeatRepository.findLiveHolds(anyLong(), anyLong(), any())).thenReturn(List.of());
        when(bookingSeatRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));

        HoldResponse response = bookingService.hold(user(), new HoldRequest(100L, List.of(1L, 2L)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BookingSeat>> captor = ArgumentCaptor.forClass(List.class);
        verify(bookingSeatRepository).saveAllAndFlush(captor.capture());
        List<BookingSeat> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getPrice()).isEqualTo(90_000);
        assertThat(saved.get(1).getPrice()).isEqualTo(120_000);
        assertThat(saved.get(0).getHoldExpiresAt())
                .isCloseTo(LocalDateTime.now().plusMinutes(10), within(5, java.time.temporal.ChronoUnit.SECONDS));
        assertThat(response.seats()).hasSize(2);
        assertThat(response.holdExpiresAt()).isEqualTo(saved.get(0).getHoldExpiresAt());
    }

    @Test
    void hold_khiDaCoHoldTruoc_keThuaHanCu() {
        Showtime s = showtimeOpen();
        LocalDateTime oldExpiry = LocalDateTime.now().plusMinutes(4);
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(s));
        when(seatRepository.findAllById(List.of(2L))).thenReturn(List.of(seat(2L, "A", 2, SeatType.STANDARD, s.getRoom())));
        when(bookingSeatRepository.findLiveHolds(anyLong(), anyLong(), any()))
                .thenReturn(List.of(holdOf(user(), s, seat(1L, "A", 1, SeatType.STANDARD, s.getRoom()), oldExpiry)));
        when(bookingSeatRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));

        HoldResponse response = bookingService.hold(user(), new HoldRequest(100L, List.of(2L)));

        assertThat(response.holdExpiresAt()).isEqualTo(oldExpiry);
        assertThat(response.seats()).hasSize(2);
    }

    @Test
    void create_khiKhongConHoldSong_nem409() {
        Showtime s = showtimeOpen();
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(s));
        when(bookingSeatRepository.findLiveHolds(anyLong(), anyLong(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> bookingService.create(user(), new CreateBookingRequest(100L, List.of())))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getMessage()).contains("hết hạn");
                });

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_snapshotDungTienVaGanBookingVaoHold() {
        Showtime s = showtimeOpen();
        User u = user();
        LocalDateTime holdExpiry = LocalDateTime.now().plusMinutes(7);
        List<BookingSeat> holds = List.of(
                holdOf(u, s, seat(1L, "A", 1, SeatType.STANDARD, s.getRoom()), holdExpiry),
                holdOf(u, s, seat(2L, "F", 1, SeatType.VIP, s.getRoom()), holdExpiry),
                holdOf(u, s, seat(3L, "H", 1, SeatType.COUPLE, s.getRoom()), holdExpiry));
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(s));
        when(bookingSeatRepository.findLiveHolds(anyLong(), anyLong(), any())).thenReturn(holds);
        when(bookingRepository.existsByCode(any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        Concession combo = new Concession();
        combo.setId(7L);
        combo.setName("Combo test");
        combo.setPrice(45_000);
        combo.setActive(true);
        when(concessionRepository.findById(7L)).thenReturn(Optional.of(combo));
        when(concessionRepository.getReferenceById(7L)).thenReturn(combo);

        BookingResponse response = bookingService.create(u,
                new CreateBookingRequest(100L, List.of(new ConcessionLine(7L, 2))));

        // 90.000 + 120.000 + 180.000 + 2 × 45.000 = 480.000
        assertThat(response.subtotal()).isEqualTo(480_000);
        assertThat(response.discount()).isZero();
        assertThat(response.total()).isEqualTo(480_000);
        assertThat(response.code()).startsWith("LVC").hasSize(11);
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        // Hold được gắn vào đơn và đồng bộ hạn giữ = hạn đơn
        for (BookingSeat hold : holds) {
            assertThat(hold.getBooking()).isNotNull();
            assertThat(hold.getHoldExpiresAt()).isEqualTo(hold.getBooking().getExpiresAt());
        }
        verify(bookingSeatRepository).saveAll(holds);
        verify(bookingConcessionRepository).saveAll(anyList());
    }

    @Test
    void cleanupExpired_chuyenDonHetHanTruocRoiMoiXoaHold() {
        bookingService.cleanupExpired();

        InOrder order = inOrder(bookingRepository, bookingSeatRepository);
        order.verify(bookingRepository).expirePendingBefore(any());
        order.verify(bookingSeatRepository).deleteExpiredHolds(any());
    }
}
