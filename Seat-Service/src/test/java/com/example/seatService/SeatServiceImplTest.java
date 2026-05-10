package com.example.seatService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.example.seatService.dto.*;
import com.example.seatService.entity.*;
import com.example.seatService.repository.SeatRepository;
import com.example.seatService.service.SeatServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    @Mock
    SeatRepository seatRepository;

    @InjectMocks
    SeatServiceImpl seatService;

    // ---------------- 1 ADD SEATS ----------------
    @Test
    void testAddSeats() {

        SeatRequestDTO dto = new SeatRequestDTO();
        dto.setFlightId(1L);
        dto.setSeatNumber("12A");
        dto.setSeatClass(SeatClass.ECONOMY);
        dto.setRowNumber(12);
        dto.setColumnLetter("A");

        when(seatRepository.saveAll(anyList()))
                .thenAnswer(i -> i.getArgument(0));

        List<SeatResponseDTO> result = seatService.addSeats(List.of(dto));

        assertEquals(1, result.size());
    }

    // ---------------- 2 GET SEATS BY FLIGHT ----------------
    @Test
    void testGetSeatsByFlight() {

        Seat seat = new Seat();
        seat.setFlightId(1L);

        when(seatRepository.findByFlightId(1L))
                .thenReturn(List.of(seat));

        List<SeatResponseDTO> result = seatService.getSeatsByFlight(1L);

        assertEquals(1, result.size());
    }

    // ---------------- 3 AVAILABLE SEATS ----------------
    @Test
    void testGetAvailableSeats() {

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.AVAILABLE);

        when(seatRepository.findByFlightIdAndStatus(1L, SeatStatus.AVAILABLE))
                .thenReturn(List.of(seat));

        List<SeatResponseDTO> result = seatService.getAvailableSeats(1L);

        assertEquals(1, result.size());
    }

    // ---------------- 4 GET BY CLASS ----------------
    @Test
    void testGetByClass() {

        Seat seat = new Seat();
        seat.setSeatClass(SeatClass.ECONOMY);

        when(seatRepository.findByFlightIdAndSeatClass(1L, SeatClass.ECONOMY))
                .thenReturn(List.of(seat));

        List<SeatResponseDTO> result =
                seatService.getByClass(1L, SeatClass.ECONOMY);

        assertEquals(1, result.size());
    }

    // ---------------- 5 HOLD SEAT SUCCESS ----------------
    @Test
    void testHoldSeat() {

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.AVAILABLE);

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A"))
                .thenReturn(Optional.of(seat));

        when(seatRepository.save(any())).thenReturn(seat);

        SeatResponseDTO result =
                seatService.holdSeat(1L, "12A", "user1");

        assertEquals(SeatStatus.HELD, seat.getStatus());
    }

    // ---------------- 6 HOLD SEAT NOT FOUND ----------------
    @Test
    void testHoldSeat_NotFound() {

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> seatService.holdSeat(1L, "12A", "user1"));

        assertEquals("Seat not found", ex.getMessage());
    }

    // ---------------- 7 HOLD SEAT NOT AVAILABLE ----------------
    @Test
    void testHoldSeat_NotAvailable() {

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.CONFIRMED);

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A"))
                .thenReturn(Optional.of(seat));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> seatService.holdSeat(1L, "12A", "user1"));

        assertEquals("Seat not available", ex.getMessage());
    }

    // ---------------- 8 RELEASE SEAT ----------------
    @Test
    void testReleaseSeat() {

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.HELD);

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A"))
                .thenReturn(Optional.of(seat));

        when(seatRepository.save(any())).thenReturn(seat);

        SeatResponseDTO result =
                seatService.releaseSeat(1L, "12A");

        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
    }

    // ---------------- 9 RELEASE NOT FOUND ----------------
    @Test
    void testReleaseSeat_NotFound() {

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> seatService.releaseSeat(1L, "12A"));

        assertEquals("Seat not found", ex.getMessage());
    }

    // ---------------- 10 RELEASE CONFIRMED NOT ALLOWED ----------------
    @Test
    void testReleaseSeat_Confirmed() {

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.CONFIRMED);

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A"))
                .thenReturn(Optional.of(seat));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> seatService.releaseSeat(1L, "12A"));

        assertEquals("Cannot release booked seat", ex.getMessage());
    }

    // ---------------- 11 CONFIRM SEAT ----------------
    @Test
    void testConfirmSeat() {

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.HELD);

        when(seatRepository.findById(1L))
                .thenReturn(Optional.of(seat));

        when(seatRepository.save(any())).thenReturn(seat);

        SeatResponseDTO result = seatService.confirmSeat(1L);

        assertEquals(SeatStatus.CONFIRMED, seat.getStatus());
    }

    // ---------------- 12 CONFIRM INVALID STATE ----------------
    @Test
    void testConfirmSeat_Invalid() {

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.AVAILABLE);

        when(seatRepository.findById(1L))
                .thenReturn(Optional.of(seat));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> seatService.confirmSeat(1L));

        assertEquals("Seat must be HELD before confirmation", ex.getMessage());
    }

    // ---------------- 13 CONFIRM NOT FOUND ----------------
    @Test
    void testConfirmSeat_NotFound() {

        when(seatRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> seatService.confirmSeat(1L));

        assertTrue(ex.getMessage().contains("Seat not found"));
    }

    // ---------------- 14 DELETE BY FLIGHT ----------------
    @Test
    void testDeleteSeatsByFlight() {

        doNothing().when(seatRepository).deleteByFlightId(1L);

        seatService.deleteSeatsByFlight(1L);

        verify(seatRepository, times(1)).deleteByFlightId(1L);
    }

    // ---------------- 15 CANCEL SEAT ----------------
    @Test
    void testCancelSeat() {

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.CONFIRMED);

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A"))
                .thenReturn(Optional.of(seat));

        when(seatRepository.save(any())).thenReturn(seat);

        seatService.cancelSeat(1L, "12A");

        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
    }
}