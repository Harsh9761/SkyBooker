package com.example.bookingService.service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    public BookingResponseDTO createBooking(BookingRequestDTO request) {

        FareSummaryDTO fare = calculateFare(request.getFlightId(), request.getLuggageKg());

        // STEP 1: HOLD SEAT (NON-BREAKING)
        try {
            System.out.println("CALLING SEAT SERVICE - HOLD");

            seatClient.holdSeat(
                    request.getFlightId(),
                    request.getSeatNumber(),
                    String.valueOf(request.getUserId())
            );

            System.out.println("SEAT HOLD SUCCESS");

        } catch (Exception e) {

            //  IMPORTANT FIX:
            // Do NOT break flow if seat already held
            System.out.println("SEAT HOLD ISSUE IGNORED: " + e.getMessage());
        }

        // STEP 2: DECREMENT FLIGHT SEAT
        try {
            flightClient.decrementSeat(request.getFlightId());
        } catch (Exception e) {
            System.out.println("FLIGHT UPDATE FAILED: " + e.getMessage());
        }

        // STEP 3: CREATE BOOKING
        try {
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

            // ROLLBACK SAFE
            try {
                seatClient.releaseSeat(
                        request.getFlightId(),
                        request.getSeatNumber()
                );

                flightClient.incrementSeat(request.getFlightId());

            } catch (Exception ex) {
                System.out.println("ROLLBACK FAILED: " + ex.getMessage());
            }

            throw new RuntimeException("Booking failed");
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
        
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Already cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        
        System.out.println(booking.getPaymentId());
        if (booking.getPaymentId() != null) {
        	try {
        	    System.out.println("Calling refund...");
        	    paymentClient.refundPayment(booking.getPaymentId());
        	} catch (Exception e) {
        	    e.printStackTrace();  //THIS WILL SHOW REAL ERROR
        	}
        }
        
        seatClient.cancelSeat(
                booking.getFlightId(),
                booking.getSeatNumber()
        );
        
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
        
        dto.setContactEmail(booking.getContactEmail());
        dto.setContactPhone(booking.getContactPhone());
        dto.setPaymentId(booking.getPaymentId());
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
    
//    @Override
//    public BookingResponseDTO completePayment(UUID paymentId,
//                                              String transactionId,
//                                              String status) {
//
//        PaymentResponseDTO res =
//                paymentClient.process(paymentId, transactionId, status);
//
//        Booking booking = bookingRepository.findById(res.getBookingId())
//                .orElseThrow(() -> new RuntimeException("Booking not found"));
//        
//        booking.setPaymentId(paymentId);
//        if ("PAID".equalsIgnoreCase(res.getStatus())) {
//
//            booking.setStatus(BookingStatus.CONFIRMED);
//
//            // FINAL LOCK ONLY ON SUCCESS
//            seatClient.lockSeat(
//                    booking.getFlightId(),
//                    booking.getSeatNumber()
//            );
//            
//
//        } else {
//
//            booking.setStatus(BookingStatus.CANCELLED);
//
//            seatClient.releaseSeat(
//                    booking.getFlightId(),
//                    booking.getSeatNumber()
//            );
//
//            flightClient.incrementSeat(booking.getFlightId());
//        }
//
//        bookingRepository.save(booking);
//
//        return mapToResponse(booking);
//    }
    
    
    @Override
    public BookingResponseDTO completePayment(UUID bookingId,
                                              UUID paymentId,
                                              String transactionId,
                                              String status) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // now set paymentId here
        booking.setPaymentId(paymentId);

        if ("PAID".equalsIgnoreCase(status)) {

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
