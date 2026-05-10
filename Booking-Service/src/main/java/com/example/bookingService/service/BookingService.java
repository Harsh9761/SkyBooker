package com.example.bookingService.service;

import java.util.List;
import java.util.UUID;

import com.example.bookingService.dto.*;

public interface BookingService {

    BookingResponseDTO createBooking(BookingRequestDTO request);

    BookingResponseDTO getBookingById(UUID bookingId);

    BookingResponseDTO getBookingByPnr(String pnr);

    List<BookingResponseDTO> getBookingsByUser(Long userId);

    List<BookingResponseDTO> getBookingsByFlight(Long flightId);

    BookingResponseDTO cancelBooking(UUID bookingId);

    FareSummaryDTO calculateFare(Long flightId, Integer luggageKg, int seats);

    BookingResponseDTO addAddOn(UUID bookingId, String meal, Integer luggageKg);

    List<BookingResponseDTO> getUpcomingBookings(Long userId);
    
    PaymentResponseDTO startPayment(UUID bookingId, String method);

    BookingResponseDTO completePayment(UUID bookingId,UUID paymentId,
                                       String transactionId,
                                       String status);
}