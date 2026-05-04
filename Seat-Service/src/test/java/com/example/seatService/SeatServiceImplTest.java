package com.example.seatService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.example.seatService.dto.SeatRequestDTO;
import com.example.seatService.entity.Seat;
import com.example.seatService.entity.SeatClass;
import com.example.seatService.entity.SeatStatus;
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

    @Test
    void testHoldSeat() {

        Long flightId = 1L;
        String seatNumber = "12A";

        Seat seat = new Seat();
        seat.setSeatId(1L);
        seat.setFlightId(flightId);
        seat.setSeatNumber(seatNumber);
        seat.setStatus(SeatStatus.AVAILABLE);

        when(seatRepository.findByFlightIdAndSeatNumber(flightId, seatNumber))
                .thenReturn(Optional.of(seat));

        when(seatRepository.save(any())).thenReturn(seat);

        var result = seatService.holdSeat(flightId, seatNumber, "user1");

        assertNotNull(result);
        assertEquals(SeatStatus.HELD, result.getStatus());
    }

    @Test
    void testReleaseSeat() {

        Seat seat = new Seat();
        seat.setFlightId(1L);
        seat.setSeatNumber("12A");
        seat.setStatus(SeatStatus.HELD);

        when(seatRepository.findByFlightIdAndSeatNumber(1L, "12A"))
                .thenReturn(Optional.of(seat));

        when(seatRepository.save(any())).thenReturn(seat);

        var result = seatService.releaseSeat(1L, "12A");

        assertEquals(SeatStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void testConfirmSeat() {

        Seat seat = new Seat();
        seat.setSeatId(1L);
        seat.setStatus(SeatStatus.HELD);

        when(seatRepository.findById(1L))
                .thenReturn(Optional.of(seat));

        when(seatRepository.save(any())).thenReturn(seat);

        var result = seatService.confirmSeat(1L);

        assertEquals(SeatStatus.CONFIRMED, result.getStatus());
    }
}