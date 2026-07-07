package com.linhvecac.catalog.showtime;

import com.linhvecac.catalog.cinema.Cinema;
import com.linhvecac.catalog.cinema.Room;
import com.linhvecac.catalog.cinema.RoomRepository;
import com.linhvecac.catalog.movie.Movie;
import com.linhvecac.catalog.movie.MovieRepository;
import com.linhvecac.catalog.showtime.dto.CinemaShowtimes;
import com.linhvecac.catalog.showtime.dto.MovieShowtimes;
import com.linhvecac.catalog.showtime.dto.ShowtimeAdminResponse;
import com.linhvecac.catalog.showtime.dto.ShowtimeRequest;
import com.linhvecac.catalog.showtime.dto.ShowtimeSlot;
import com.linhvecac.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    /** Đệm dọn phòng giữa các suất (phút) — cộng vào ends_at khi tính giờ kết thúc. */
    private static final int CLEANUP_BUFFER_MIN = 15;

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    /** Suất chiếu của 1 phim trong 1 ngày, gộp theo rạp — dùng cho trang chi tiết phim. */
    @Transactional(readOnly = true)
    public List<CinemaShowtimes> listByMovieAndDate(Long movieId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        List<Showtime> showtimes =
                showtimeRepository.findByMovieIdAndStartsAtBetweenOrderByStartsAtAsc(movieId, from, to);

        Map<Long, CinemaShowtimes> grouped = new LinkedHashMap<>();
        Map<Long, List<ShowtimeSlot>> slots = new LinkedHashMap<>();
        for (Showtime s : showtimes) {
            Cinema cinema = s.getRoom().getCinema();
            grouped.computeIfAbsent(cinema.getId(), id -> {
                slots.put(id, new ArrayList<>());
                return new CinemaShowtimes(cinema.getId(), cinema.getName(), cinema.getAddress(),
                        cinema.getChain().getName(), cinema.getRegion().getId(), slots.get(id));
            });
            slots.get(cinema.getId()).add(ShowtimeSlot.from(s));
        }
        return new ArrayList<>(grouped.values());
    }

    /** Suất chiếu của 1 rạp trong 1 ngày, gộp theo phim — dùng cho bước 3 wizard đặt vé. */
    @Transactional(readOnly = true)
    public List<MovieShowtimes> listByCinemaAndDate(Long cinemaId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        List<Showtime> showtimes =
                showtimeRepository.findByRoom_Cinema_IdAndStartsAtBetweenOrderByStartsAtAsc(cinemaId, from, to);

        Map<Long, MovieShowtimes> grouped = new LinkedHashMap<>();
        Map<Long, List<ShowtimeSlot>> slots = new LinkedHashMap<>();
        for (Showtime s : showtimes) {
            if (s.getStatus() != ShowtimeStatus.OPEN || !s.getStartsAt().isAfter(now)) {
                continue;
            }
            Movie movie = s.getMovie();
            grouped.computeIfAbsent(movie.getId(), id -> {
                slots.put(id, new ArrayList<>());
                return new MovieShowtimes(movie.getId(), movie.getTitle(), movie.getPosterUrl(),
                        movie.getAgeRating(), movie.getDurationMin(), slots.get(id));
            });
            slots.get(movie.getId()).add(ShowtimeSlot.from(s));
        }
        return new ArrayList<>(grouped.values());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeAdminResponse> adminList(Long cinemaId, Long roomId, LocalDate date) {
        LocalDate day = (date != null) ? date : LocalDate.now();
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = day.plusDays(1).atStartOfDay();
        List<Showtime> result;
        if (roomId != null) {
            result = showtimeRepository.findByRoomIdAndStartsAtBetweenOrderByStartsAtAsc(roomId, from, to);
        } else if (cinemaId != null) {
            result = showtimeRepository.findByRoom_Cinema_IdAndStartsAtBetweenOrderByStartsAtAsc(cinemaId, from, to);
        } else {
            result = showtimeRepository.findByStartsAtBetweenOrderByStartsAtAsc(from, to);
        }
        return result.stream().map(ShowtimeAdminResponse::from).toList();
    }

    @Transactional
    public ShowtimeAdminResponse create(ShowtimeRequest request) {
        Showtime showtime = new Showtime();
        apply(showtime, request, null);
        return ShowtimeAdminResponse.from(showtimeRepository.save(showtime));
    }

    @Transactional
    public ShowtimeAdminResponse update(Long id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy suất chiếu"));
        apply(showtime, request, id);
        return ShowtimeAdminResponse.from(showtimeRepository.save(showtime));
    }

    @Transactional
    public void delete(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy suất chiếu");
        }
        showtimeRepository.deleteById(id);
    }

    private void apply(Showtime showtime, ShowtimeRequest r, Long excludeId) {
        Movie movie = movieRepository.findById(r.movieId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy phim"));
        Room room = roomRepository.findById(r.roomId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng chiếu"));

        LocalDateTime endsAt = r.startsAt().plusMinutes(movie.getDurationMin() + CLEANUP_BUFFER_MIN);
        if (showtimeRepository.countOverlapping(room.getId(), r.startsAt(), endsAt, excludeId) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Phòng đã có suất chiếu trùng khung giờ");
        }

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartsAt(r.startsAt());
        showtime.setEndsAt(endsAt);
        showtime.setBasePrice(r.basePrice());
        showtime.setStatus(r.status() != null ? r.status() : ShowtimeStatus.OPEN);
    }
}
