package com.example.bookingService.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.example.bookingService.dto.*;
import com.example.bookingService.service.BookingService;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // CREATE BOOKING
    @PostMapping
    public BookingResponseDTO createBooking(@RequestBody BookingRequestDTO request) {
        return bookingService.createBooking(request);
    }

    // GET BY ID 
    @GetMapping("/{bookingId}")
    public BookingResponseDTO getById(@PathVariable UUID bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    // GET BY PNR 
    @GetMapping("/pnr/{pnr}")
    public BookingResponseDTO getByPnr(@PathVariable String pnr) {
        return bookingService.getBookingByPnr(pnr);
    }

    // GET BY USER
    @GetMapping("/user/{userId}")
    public List<BookingResponseDTO> getByUser(@PathVariable Long userId) {
        return bookingService.getBookingsByUser(userId);
    }

    //GET BY FLIGHT
    @GetMapping("/flight/{flightId}")
    public List<BookingResponseDTO> getByFlight(@PathVariable Long flightId) {
        return bookingService.getBookingsByFlight(flightId);
    }

    // CANCEL BOOKING
    @PutMapping("/{bookingId}/cancel")
    public BookingResponseDTO cancelBooking(@PathVariable UUID bookingId) {
        return bookingService.cancelBooking(bookingId);
    }

    // CALCULATE FARE
    @GetMapping("/fare")
    public FareSummaryDTO calculateFare(@RequestParam Long flightId,
                                        @RequestParam(required = false) Integer luggageKg) {
        return bookingService.calculateFare(flightId, luggageKg);
    }

    //  ADD ONS 
    @PutMapping("/{bookingId}/addon")
    public BookingResponseDTO addAddOn(@PathVariable UUID bookingId,
                                        @RequestParam String meal,
                                        @RequestParam Integer luggageKg) {
        return bookingService.addAddOn(bookingId, meal, luggageKg);
    }

    // UPCOMING BOOKINGS
    @GetMapping("/upcoming/{userId}")
    public List<BookingResponseDTO> getUpcoming(@PathVariable Long userId) {
        return bookingService.getUpcomingBookings(userId);
    }
}