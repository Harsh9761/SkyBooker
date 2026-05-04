package com.example.seatService.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.seatService.entity.Seat;
import com.example.seatService.entity.SeatStatus;
import com.example.seatService.repository.SeatRepository;

@Component
public class SeatScheduler {

    @Autowired
    private SeatRepository seatRepository;

    @Scheduled(fixedRate = 30000) // every 30 sec
    public void releaseExpiredSeats() {

        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(2);

        List<Seat> seats = seatRepository
                .findByStatusAndHeldAtBefore(SeatStatus.HELD, expiryTime);

        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setHeldAt(null);
            seatRepository.save(seat);
        }
    }
}