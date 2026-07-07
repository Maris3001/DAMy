package com.linhvecac.catalog.showtime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieIdAndStartsAtBetweenOrderByStartsAtAsc(
            Long movieId, LocalDateTime from, LocalDateTime to);

    List<Showtime> findByRoom_Cinema_IdAndStartsAtBetweenOrderByStartsAtAsc(
            Long cinemaId, LocalDateTime from, LocalDateTime to);

    List<Showtime> findByRoomIdAndStartsAtBetweenOrderByStartsAtAsc(
            Long roomId, LocalDateTime from, LocalDateTime to);

    List<Showtime> findByStartsAtBetweenOrderByStartsAtAsc(LocalDateTime from, LocalDateTime to);

    /** Suất chiếu khác (loại trừ chính nó) trong cùng phòng có khung giờ giao với [start, end). */
    @Query("""
            SELECT COUNT(s) FROM Showtime s
            WHERE s.room.id = :roomId
              AND (:excludeId IS NULL OR s.id <> :excludeId)
              AND s.startsAt < :end
              AND s.endsAt > :start
            """)
    long countOverlapping(@Param("roomId") Long roomId,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end,
                          @Param("excludeId") Long excludeId);
}
