package com.example.seatService;


import com.example.seatService.dto.SeatRequestDTO;
import com.example.seatService.entity.*;
import com.example.seatService.repository.SeatRepository;
import com.example.seatService.service.SeatServiceImpl;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatServiceTest {

    @InjectMocks
    private SeatServiceImpl seatService;

    @Mock
    private SeatRepository seatRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddSeats() {

        SeatRequestDTO dto = new SeatRequestDTO();
        dto.setFlightId(101L);
        dto.setSeatNumber("1A");
        dto.setSeatClass(SeatClass.ECONOMY);
        dto.setRowNumber(1);
        dto.setColumnLetter("A");
        dto.setPriceMultiplier(1.0);

        Seat seat = new Seat();
        seat.setSeatId(1L);
        seat.setSeatNumber("1A");
        seat.setSeatClass(SeatClass.ECONOMY);
        seat.setStatus(SeatStatus.AVAILABLE);

        when(seatRepository.saveAll(any())).thenReturn(List.of(seat));

        var result = seatService.addSeats(List.of(dto));

        assertEquals(1, result.size());
    }

    @Test
    void testGetSeatsByFlight() {

        Seat seat = new Seat();
        seat.setSeatId(1L);
        seat.setFlightId(101L);

        when(seatRepository.findByFlightId(101L))
                .thenReturn(List.of(seat));

        var result = seatService.getSeatsByFlight(101L);

        assertEquals(1, result.size());
    }

    @Test
    void testHoldSeat() {

        Seat seat = new Seat();
        seat.setSeatId(1L);
        seat.setStatus(SeatStatus.AVAILABLE);

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any())).thenReturn(seat);

        var result = seatService.holdSeat(1L);

        assertEquals(SeatStatus.HELD, result.getStatus());
    }

    @Test
    void testReleaseSeat() {

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.HELD);

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any())).thenReturn(seat);

        var result = seatService.releaseSeat(1L);

        assertEquals(SeatStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void testConfirmSeat() {

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.HELD);

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any())).thenReturn(seat);

        var result = seatService.confirmSeat(1L);

        assertEquals(SeatStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void testSeatNotFound() {

        when(seatRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> seatService.holdSeat(99L));

        assertTrue(ex.getMessage().contains("not found"));
    }
}
