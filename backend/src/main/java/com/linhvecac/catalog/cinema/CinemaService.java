package com.linhvecac.catalog.cinema;

import com.linhvecac.catalog.cinema.dto.ChainResponse;
import com.linhvecac.catalog.cinema.dto.CinemaRequest;
import com.linhvecac.catalog.cinema.dto.CinemaResponse;
import com.linhvecac.catalog.cinema.dto.GenerateSeatsRequest;
import com.linhvecac.catalog.cinema.dto.RoomRequest;
import com.linhvecac.catalog.cinema.dto.RoomResponse;
import com.linhvecac.catalog.cinema.dto.SeatResponse;
import com.linhvecac.catalog.cinema.dto.SeatUpdateRequest;
import com.linhvecac.catalog.region.Region;
import com.linhvecac.catalog.region.RegionRepository;
import com.linhvecac.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaChainRepository chainRepository;
    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final RegionRepository regionRepository;

    // ===== Chains =====
    @Transactional(readOnly = true)
    public List<ChainResponse> listChains() {
        return chainRepository.findAllByOrderByNameAsc().stream().map(ChainResponse::from).toList();
    }

    // ===== Cinemas =====
    @Transactional(readOnly = true)
    public List<CinemaResponse> listCinemas(Long regionId) {
        List<Cinema> cinemas = (regionId != null)
                ? cinemaRepository.findByRegionIdOrderByNameAsc(regionId)
                : cinemaRepository.findAllByOrderByNameAsc();
        return cinemas.stream().map(CinemaResponse::from).toList();
    }

    @Transactional
    public CinemaResponse createCinema(CinemaRequest r) {
        Cinema c = new Cinema();
        applyCinema(c, r);
        return CinemaResponse.from(cinemaRepository.save(c));
    }

    @Transactional
    public CinemaResponse updateCinema(Long id, CinemaRequest r) {
        Cinema c = getCinema(id);
        applyCinema(c, r);
        return CinemaResponse.from(cinemaRepository.save(c));
    }

    @Transactional
    public void deleteCinema(Long id) {
        Cinema c = getCinema(id);
        if (!roomRepository.findByCinemaIdOrderByNameAsc(id).isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Vui lòng xóa các phòng chiếu của rạp trước");
        }
        cinemaRepository.delete(c);
    }

    private void applyCinema(Cinema c, CinemaRequest r) {
        CinemaChain chain = chainRepository.findById(r.chainId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy hãng rạp"));
        Region region = regionRepository.findById(r.regionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khu vực"));
        c.setChain(chain);
        c.setRegion(region);
        c.setName(r.name().trim());
        c.setAddress(r.address().trim());
    }

    private Cinema getCinema(Long id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy rạp"));
    }

    // ===== Rooms =====
    @Transactional(readOnly = true)
    public List<RoomResponse> listRooms(Long cinemaId) {
        List<Room> rooms = roomRepository.findByCinemaIdOrderByNameAsc(cinemaId);
        return rooms.stream()
                .map(room -> RoomResponse.from(room, seatRepository.countByRoomId(room.getId())))
                .toList();
    }

    @Transactional
    public RoomResponse createRoom(RoomRequest r) {
        Room room = new Room();
        applyRoom(room, r);
        return RoomResponse.from(roomRepository.save(room), 0);
    }

    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest r) {
        Room room = getRoom(id);
        applyRoom(room, r);
        return RoomResponse.from(roomRepository.save(room), seatRepository.countByRoomId(id));
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = getRoom(id);
        seatRepository.deleteByRoomId(id);
        roomRepository.delete(room);
    }

    private void applyRoom(Room room, RoomRequest r) {
        Cinema cinema = getCinema(r.cinemaId());
        room.setCinema(cinema);
        room.setName(r.name().trim());
        room.setRoomType(r.roomType() != null && !r.roomType().isBlank() ? r.roomType() : "2D");
    }

    private Room getRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng chiếu"));
    }

    // ===== Seats =====
    @Transactional(readOnly = true)
    public List<SeatResponse> listSeats(Long roomId) {
        getRoom(roomId);
        return seatRepository.findByRoomIdOrderByRowLabelAscColNumberAsc(roomId)
                .stream().map(SeatResponse::from).toList();
    }

    /** Sinh lưới ghế rows×cols cho phòng: xóa toàn bộ ghế cũ rồi tạo mới (mặc định STANDARD). */
    @Transactional
    public List<SeatResponse> generateSeats(Long roomId, GenerateSeatsRequest r) {
        Room room = getRoom(roomId);
        seatRepository.deleteByRoomId(roomId);
        seatRepository.flush();
        List<Seat> seats = new ArrayList<>();
        for (int rowIdx = 0; rowIdx < r.rows(); rowIdx++) {
            String label = String.valueOf((char) ('A' + rowIdx));
            for (int col = 1; col <= r.cols(); col++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setRowLabel(label);
                seat.setColNumber(col);
                seat.setSeatType(SeatType.STANDARD);
                seats.add(seat);
            }
        }
        return seatRepository.saveAll(seats).stream().map(SeatResponse::from).toList();
    }

    /** Cập nhật loại ghế hàng loạt; chỉ chấp nhận ghế thuộc đúng phòng. */
    @Transactional
    public List<SeatResponse> updateSeats(Long roomId, SeatUpdateRequest request) {
        getRoom(roomId);
        List<Seat> seats = seatRepository.findByRoomIdOrderByRowLabelAscColNumberAsc(roomId);
        Map<Long, Seat> byId = seats.stream().collect(Collectors.toMap(Seat::getId, Function.identity()));
        for (SeatUpdateRequest.SeatTypeChange change : request.seats()) {
            Seat seat = byId.get(change.seatId());
            if (seat == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Ghế không thuộc phòng này");
            }
            seat.setSeatType(change.seatType());
        }
        return seatRepository.saveAll(seats).stream().map(SeatResponse::from).toList();
    }
}
