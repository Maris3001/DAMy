package com.linhvecac.catalog.cinema;

import com.linhvecac.catalog.cinema.dto.GenerateSeatsRequest;
import com.linhvecac.catalog.cinema.dto.SeatResponse;
import com.linhvecac.catalog.region.RegionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock
    private CinemaChainRepository chainRepository;
    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private CinemaService cinemaService;

    @Test
    void generateSeats_sinhDungSoLuongVaNhanHang() {
        Room room = new Room();
        room.setId(5L);
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(seatRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<SeatResponse> seats = cinemaService.generateSeats(5L, new GenerateSeatsRequest(3, 4));

        // 3 hàng × 4 cột = 12 ghế; xóa ghế cũ trước khi sinh
        verify(seatRepository).deleteByRoomId(5L);
        assertThat(seats).hasSize(12);
        assertThat(seats).allMatch(s -> s.seatType() == SeatType.STANDARD);
        assertThat(seats).extracting(SeatResponse::rowLabel).contains("A", "B", "C");
        assertThat(seats).noneMatch(s -> s.rowLabel().equals("D"));
        assertThat(seats).filteredOn(s -> s.rowLabel().equals("A")).hasSize(4);
    }
}
