package com.example.bookingService.service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bookingService.client.FlightClient;
import com.example.bookingService.client.PaymentClient;
import com.example.bookingService.client.SeatClient;
import com.example.bookingService.dto.*;
import com.example.bookingService.entity.*;
import com.example.bookingService.repository.BookingRepository;


@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentClient paymentClient;

    public BookingServiceImpl(BookingRepository bookingRepository,PaymentClient paymentClient) {
        this.bookingRepository = bookingRepository;
        this.paymentClient = paymentClient;
    }
    
    @Autowired
    private SeatClient seatClient;
    
    @Autowired
    private FlightClient flightClient;

    //CREATE BOOKING
    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO request) {

        // Fare calculate
        FareSummaryDTO fare = calculateFare(request.getFlightId(), request.getLuggageKg());

        try {
            System.out.println("CALLING SEAT SERVICE");

            // HOLD seat
            seatClient.holdSeat(
                    request.getFlightId(),
                    request.getSeatNumber(),
                    String.valueOf(request.getUserId())
            );

            // Flight seat count decrease
            flightClient.decrementSeat(request.getFlightId());

            System.out.println("SEAT HOLD SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Seat hold failed");
        }

        try {
            // Create booking
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

        } catch (Exception e) {

            // ROLLBACK (VERY IMPORTANT)
            try {
                seatClient.releaseSeat(
                        request.getFlightId(),
                        request.getSeatNumber()
                );

                flightClient.incrementSeat(request.getFlightId());

            } catch (Exception ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }

            throw new RuntimeException("Booking failed, rollback done");
        }
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
    
    
    public PaymentResponseDTO startPayment(UUID bookingId, String method) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Booking not in valid state");
        }

        PaymentRequestDTO req = new PaymentRequestDTO();

        req.setBookingId(bookingId);
        req.setUserId(booking.getUserId());
        req.setAmount(booking.getTotalFare());
        req.setCurrency("INR");

        try {
            req.setPaymentMode(PaymentMode.valueOf(method.toUpperCase()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid payment method");
        }

        return paymentClient.initiate(req);
    }
    
    @Override
    public BookingResponseDTO completePayment(UUID paymentId,
                                              String transactionId,
                                              String status) {

        PaymentResponseDTO res =
                paymentClient.process(paymentId, transactionId, status);

        Booking booking = bookingRepository.findById(res.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        //SUCCESS CASE
        if ("PAID".equalsIgnoreCase(res.getStatus())) {

            booking.setStatus(BookingStatus.CONFIRMED);

            
            seatClient.lockSeat(
                    booking.getFlightId(),
                    booking.getSeatNumber()
            );

        } else {

            
            booking.setStatus(BookingStatus.CANCELLED);

            seatClient.releaseSeat(
                    booking.getFlightId(),
                    booking.getSeatNumber()
            );

            flightClient.incrementSeat(booking.getFlightId());
        }

        bookingRepository.save(booking);

        return mapToResponse(booking);
    }
}
