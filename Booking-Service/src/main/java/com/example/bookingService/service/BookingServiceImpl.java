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

    public BookingServiceImpl(BookingRepository bookingRepository,
                              PaymentClient paymentClient) {
        this.bookingRepository = bookingRepository;
        this.paymentClient = paymentClient;
    }

    @Autowired
    private SeatClient seatClient;

    @Autowired
    private FlightClient flightClient;

   
    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO request) {

        if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
            throw new RuntimeException("No seats selected");
        }

        Set<String> unique = new HashSet<>(request.getSeatNumbers());
        if (unique.size() != request.getSeatNumbers().size()) {
            throw new RuntimeException("Duplicate seats selected");
        }

        int totalSeats = request.getSeatNumbers().size();

        FareSummaryDTO fare = calculateFare(
                request.getFlightId(),
                request.getLuggageKg(),
                totalSeats
        );

        List<String> heldSeats = new ArrayList<>();
        boolean seatHoldSuccess = false;

        try {
            // HOLD SEATS
            for (String seat : request.getSeatNumbers()) {
                seatClient.holdSeat(
                        request.getFlightId(),
                        seat,
                        String.valueOf(request.getUserId())
                );
                heldSeats.add(seat);
            }
            seatHoldSuccess = true;

            // UPDATE FLIGHT SEATS
            flightClient.decrementSeatBulk(
                    request.getFlightId(),
                    totalSeats
            );

            // SAVE BOOKING
            Booking booking = new Booking();

            booking.setUserId(request.getUserId());
            booking.setFlightId(request.getFlightId());
//            booking.setSeatNumber(request.getSeatNumbers());
            booking.setSeatNumbers(
            	    String.join(",", request.getSeatNumbers())
            	);

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

            return mapToResponse(bookingRepository.save(booking));

        } catch (Exception e) {

            if (seatHoldSuccess) {
                for (String s : heldSeats) {
                    seatClient.releaseSeat(request.getFlightId(), s);
                }
            }

            throw new RuntimeException("Booking failed: " + e.getMessage());
        }
    }

   
    @Override
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
    public BookingResponseDTO completePayment(UUID bookingId,
                                              UUID paymentId,
                                              String transactionId,
                                              String status) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setPaymentId(paymentId);

        if ("PAID".equalsIgnoreCase(status)) {

            booking.setStatus(BookingStatus.CONFIRMED);

//            for (String seat : booking.getSeatNumbers()) {
//                seatClient.lockSeat(booking.getFlightId(), seat);
//            }
            
            String[] seats = booking.getSeatNumbers().split(",");

            for (String seat : seats) {
                seatClient.lockSeat(booking.getFlightId(), seat);
            }

        } else {

            booking.setStatus(BookingStatus.CANCELLED);

//            for (String seat : booking.getSeatNumbers()) {
//                seatClient.releaseSeat(booking.getFlightId(), seat);
//            }
            
            String[] seats = booking.getSeatNumbers().split(",");

            for (String seat : seats) {
                seatClient.cancelSeat(booking.getFlightId(), seat);
            }

            flightClient.incrementSeatBulk(
                    booking.getFlightId(),
                    seats.length
            );
        }

        bookingRepository.save(booking);
        return mapToResponse(booking);
    }

  
    @Override
    public BookingResponseDTO cancelBooking(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        // PAYMENT REFUND (safe)
        if (booking.getPaymentId() != null) {
            try {
                paymentClient.refundPayment(booking.getPaymentId());
            } catch (Exception e) {
                System.out.println("Refund failed: " + e.getMessage());
            }
        }

        // SEAT RELEASE (SAFE BLOCK)
        if (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()) {

            String[] seats = booking.getSeatNumbers().split(",");

            for (String seat : seats) {
                try {
                    seatClient.cancelSeat(booking.getFlightId(), seat.trim());
                } catch (Exception e) {
                    System.out.println("Seat release failed for " + seat + ": " + e.getMessage());
                }
            }
        }

        // FLIGHT UPDATE (SAFE)
        try {
            if (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()) {

                String[] seats = booking.getSeatNumbers().split(",");

                flightClient.incrementSeatBulk(
                        booking.getFlightId(),
                        seats.length
                );
            }
        } catch (Exception e) {
            System.out.println("Flight update failed: " + e.getMessage());
        }

        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public FareSummaryDTO calculateFare(Long flightId,
                                        Integer luggageKg,
                                        int seats) {

        double baseFare = 5000.0 * seats;
        double tax = baseFare * 0.18;
        double luggageCharge = (luggageKg != null ? luggageKg * 50.0 : 0);

        double total = baseFare + tax + luggageCharge;

        FareSummaryDTO dto = new FareSummaryDTO();
        dto.setBaseFare(baseFare);
        dto.setTaxes(tax);
        dto.setTotalFare(total);

        return dto;
    }

    
    @Override
    public BookingResponseDTO getBookingById(UUID bookingId) {
        return mapToResponse(
                bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Booking not found"))
        );
    }

    @Override
    public BookingResponseDTO getBookingByPnr(String pnr) {
        return mapToResponse(
                bookingRepository.findByPnrCode(pnr)
                        .orElseThrow(() -> new RuntimeException("PNR not found"))
        );
    }

    @Override
    public List<BookingResponseDTO> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDTO> getBookingsByFlight(Long flightId) {
        return bookingRepository.findByFlightId(flightId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

  
    @Override
    public BookingResponseDTO addAddOn(UUID bookingId, String meal, Integer luggageKg) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setMealPreference(meal);
        booking.setLuggageKg(luggageKg);

        return mapToResponse(bookingRepository.save(booking));
    }

    
    @Override
    public List<BookingResponseDTO> getUpcomingBookings(Long userId) {

        return bookingRepository.findByUserId(userId)
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    
    private String generatePnr() {
        return UUID.randomUUID()
                .toString()
                .replaceAll("-", "")
                .substring(0, 6)
                .toUpperCase();
    }

    private BookingResponseDTO mapToResponse(Booking booking) {

        BookingResponseDTO dto = new BookingResponseDTO();

        dto.setBookingId(booking.getBookingId());
        dto.setPnrCode(booking.getPnrCode());
        dto.setStatus(booking.getStatus().name());
        dto.setTotalFare(booking.getTotalFare());
//        dto.setSeatNumbers(booking.getSeatNumbers());
        dto.setSeatNumbers(
        	    booking.getSeatNumbers() == null
        	        ? new ArrayList<>()
        	        : Arrays.asList(booking.getSeatNumbers().split(","))
        	);

        dto.setContactEmail(booking.getContactEmail());
        dto.setContactPhone(booking.getContactPhone());
        dto.setPaymentId(booking.getPaymentId());

        return dto;
    }
}