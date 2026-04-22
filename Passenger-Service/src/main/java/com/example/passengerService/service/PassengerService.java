package com.example.passengerService.service;

import com.example.passengerService.dto.PassengerDTO;
import java.util.List;

public interface PassengerService {

    PassengerDTO addPassenger(PassengerDTO dto);

    PassengerDTO getPassengerById(Long passengerId);

    List<PassengerDTO> getPassengersByBooking(Long bookingId);

    PassengerDTO getByPassportNumber(String passportNumber);

    PassengerDTO updatePassenger(Long passengerId, PassengerDTO dto);

    PassengerDTO assignSeat(Long passengerId, Long seatId, String seatNumber);

    String generateTicketNumber();

    long getPassengerCount(Long bookingId);

    void deletePassenger(Long passengerId);
}