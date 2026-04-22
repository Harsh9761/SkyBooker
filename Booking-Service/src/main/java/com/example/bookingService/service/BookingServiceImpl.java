package com.example.bookingService.service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bookingService.client.SeatClient;
import com.example.bookingService.dto.*;
import com.example.bookingService.entity.*;
import com.example.bookingService.repository.BookingRepository;


@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
    
    @Autowired
    private SeatClient seatClient;

    //CREATE BOOKING
    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO request) {

        FareSummaryDTO fare = calculateFare(request.getFlightId(), request.getLuggageKg());
        
        try {
            System.out.println("CALLING SEAT SERVICE");

            seatClient.holdSeat(
                request.getFlightId(),
                request.getSeatNumber(),
                String.valueOf(request.getUserId())
            );
           
            System.out.println("SEAT SERVICE CALLED SUCCESSFULLY");

        } catch (Exception e) {
            e.printStackTrace(); //  VERY IMPORTANT
            throw new RuntimeException(e.getMessage());
        }

        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setFlightId(request.getFlightId());
        booking.setSeatNumber(request.getSeatNumber());
        booking.setPnrCode(generatePnr());
        booking.setTripType(TripType.valueOf(request.getTripType()));
        booking.setStatus(BookingStatus.PENDING);

        booking.setBaseFare(fare.getBaseFare());
        booking.setTaxes(fare.getTaxes());
        booking.setTotalFare(fare.getTotalFare());

        booking.setMealPreference(request.getMealPreference());
        booking.setLuggageKg(request.getLuggageKg());

        booking.setContactEmail(request.getContactEmail());
        booking.setContactPhone(request.getContactPhone());

        booking.setBookedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        return mapToResponse(saved);
    }

    // GET BY ID 
    @Override
    public BookingResponseDTO getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return mapToResponse(booking);
    }

    // GET BY PNR 
    @Override
    public BookingResponseDTO getBookingByPnr(String pnr) {
        Booking booking = bookingRepository.findByPnrCode(pnr)
                .orElseThrow(() -> new RuntimeException("PNR not found"));

        return mapToResponse(booking);
    }

    // USER BOOKINGS-
    @Override
    public List<BookingResponseDTO> getBookingsByUser(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        return bookings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // FLIGHT BOOKINGS
    @Override
    public List<BookingResponseDTO> getBookingsByFlight(Long flightId) {
        List<Booking> bookings = bookingRepository.findByFlightId(flightId);

        return bookings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //CANCEL BOOKING
    @Override
    public BookingResponseDTO cancelBooking(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);
        
        seatClient.releaseSeat(booking.getFlightId(), booking.getSeatNumber());
        Booking updated = bookingRepository.save(booking);

        return mapToResponse(updated);
    }

    //CALCULATE FARE
    @Override
    public FareSummaryDTO calculateFare(Long flightId, Integer luggageKg) {

        double baseFare = 5000.0;
        double tax = baseFare * 0.18;
        double luggageCharge = (luggageKg != null ? luggageKg * 50.0 : 0);

        double total = baseFare + tax + luggageCharge;

        FareSummaryDTO dto = new FareSummaryDTO();
        dto.setBaseFare(baseFare);
        dto.setTaxes(tax);
        dto.setTotalFare(total);

        return dto;
    }

    // ADD ONS
    @Override
    public BookingResponseDTO addAddOn(UUID bookingId, String meal, Integer luggageKg) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setMealPreference(meal);
        booking.setLuggageKg(luggageKg);

        Booking updated = bookingRepository.save(booking);

        return mapToResponse(updated);
    }

    // UPCOMING BOOKINGS
    @Override
    public List<BookingResponseDTO> getUpcomingBookings(Long userId) {

        List<Booking> bookings = bookingRepository.findByUserId(userId);

        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //PN GENERATOR 
    private String generatePnr() {
        return UUID.randomUUID()
                .toString()
                .replaceAll("-", "")
                .substring(0, 6)
                .toUpperCase();
    }

    // MAPPER 
    private BookingResponseDTO mapToResponse(Booking booking) {

        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setBookingId(booking.getBookingId());
        dto.setPnrCode(booking.getPnrCode());
        dto.setStatus(booking.getStatus().name());
        dto.setTotalFare(booking.getTotalFare());
        dto.setSeatNumber(booking.getSeatNumber());
        return dto;
    }
}
