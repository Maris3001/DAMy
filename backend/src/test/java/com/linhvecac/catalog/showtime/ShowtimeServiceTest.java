package com.linhvecac.catalog.showtime;

import com.linhvecac.catalog.cinema.Cinema;
import com.linhvecac.catalog.cinema.Room;
import com.linhvecac.catalog.cinema.RoomRepository;
import com.linhvecac.catalog.movie.Movie;
import com.linhvecac.catalog.movie.MovieRepository;
import com.linhvecac.catalog.showtime.dto.ShowtimeRequest;
import com.linhvecac.common.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ShowtimeService showtimeService;

    private final LocalDateTime start = LocalDateTime.of(2026, 8, 1, 19, 0);

    private Movie movie120() {
        Movie m = new Movie();
        m.setId(1L);
        m.setTitle("Phim test");
        m.setDurationMin(120);
        return m;
    }

    private Room room() {
        Cinema c = new Cinema();
        c.setId(9L);
        c.setName("CGV Test");
        Room r = new Room();
        r.setId(5L);
        r.setName("Phòng 1");
        r.setRoomType("2D");
        r.setCinema(c);
        return r;
    }

    @Test
    void create_khiTrungGioPhong_neném409() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie120()));
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room()));
        // ends_at = 19:00 + 120 + 15 = 21:15; giả lập đã có suất trùng
        when(showtimeRepository.countOverlapping(eq(5L), eq(start),
                eq(start.plusMinutes(135)), eq(null))).thenReturn(1L);

        ShowtimeRequest req = new ShowtimeRequest(1L, 5L, start, 90000L, ShowtimeStatus.OPEN);

        assertThatThrownBy(() -> showtimeService.create(req))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void create_khiKhongTrung_luuVoiEndsAtTinhTuThoiLuong() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie120()));
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room()));
        when(showtimeRepository.countOverlapping(any(), any(), any(), any())).thenReturn(0L);
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(inv -> inv.getArgument(0));

        ShowtimeRequest req = new ShowtimeRequest(1L, 5L, start, 90000L, ShowtimeStatus.OPEN);
        showtimeService.create(req);

        ArgumentCaptor<Showtime> captor = ArgumentCaptor.forClass(Showtime.class);
        verify(showtimeRepository).save(captor.capture());
        Showtime saved = captor.getValue();
        assertThat(saved.getStartsAt()).isEqualTo(start);
        assertThat(saved.getEndsAt()).isEqualTo(start.plusMinutes(135));
        assertThat(saved.getBasePrice()).isEqualTo(90000L);
    }
}
