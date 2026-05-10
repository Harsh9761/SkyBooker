package com.example.bookingService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.example.bookingService.client.FlightClient;
import com.example.bookingService.client.PaymentClient;
import com.example.bookingService.client.SeatClient;
import com.example.bookingService.dto.*;
import com.example.bookingService.entity.*;
import com.example.bookingService.repository.BookingRepository;
import com.example.bookingService.service.BookingServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    BookingRepository repository;

    @Mock
    PaymentClient paymentClient;

    @Mock
    SeatClient seatClient;

    @Mock
    FlightClient flightClient;

    @InjectMocks
    BookingServiceImpl service;

    @Test
    void testCalculateFare() {
        FareSummaryDTO result = service.calculateFare(1L, 10, 2);
        assertTrue(result.getTotalFare() > 0);
    }


    @Test
    void testGetBookingById_NotFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.getBookingById(UUID.randomUUID()));
    }


    @Test
    void testAddAddOn_NotFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.addAddOn(UUID.randomUUID(), "Veg", 10));
    }

    @Test
    void testCreateBooking_NoSeats() {
        BookingRequestDTO req = new BookingRequestDTO();
        req.setSeatNumbers(Collections.emptyList());
        assertThrows(RuntimeException.class,
                () -> service.createBooking(req));
    }

    @Test
    void testCreateBooking_DuplicateSeats() {
        BookingRequestDTO req = new BookingRequestDTO();
        req.setSeatNumbers(List.of("A1", "A1"));
        assertThrows(RuntimeException.class,
                () -> service.createBooking(req));
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {
        Booking b = new Booking();
        b.setBookingId(UUID.randomUUID());
        b.setStatus(BookingStatus.CANCELLED);
        when(repository.findById(any())).thenReturn(Optional.of(b));
        assertThrows(RuntimeException.class,
                () -> service.cancelBooking(b.getBookingId()));
    }

    @Test
    void testCancelBooking_Success() {
        Booking b = new Booking();
        b.setBookingId(UUID.randomUUID());
        b.setStatus(BookingStatus.CONFIRMED);
        b.setSeatNumbers("A1,A2");
        when(repository.findById(any())).thenReturn(Optional.of(b));
        when(repository.save(any())).thenReturn(b);
        BookingResponseDTO result = service.cancelBooking(b.getBookingId());
        assertNotNull(result);
    }

    @Test
    void testStartPayment() {
        Booking b = new Booking();
        b.setBookingId(UUID.randomUUID());
        b.setStatus(BookingStatus.PENDING);
        b.setUserId(1L);
        b.setTotalFare(1000.0);
        when(repository.findById(any())).thenReturn(Optional.of(b));
        when(paymentClient.initiate(any())).thenReturn(new PaymentResponseDTO());
        PaymentResponseDTO result = service.startPayment(b.getBookingId(), "CARD");
        assertNotNull(result);
    }

    @Test
    void testStartPayment_InvalidState() {
        Booking b = new Booking();
        b.setStatus(BookingStatus.CONFIRMED);
        when(repository.findById(any())).thenReturn(Optional.of(b));
        assertThrows(RuntimeException.class,
                () -> service.startPayment(UUID.randomUUID(), "CARD"));
    }

    @Test
    void testStartPayment_InvalidMethod() {
        Booking b = new Booking();
        b.setStatus(BookingStatus.PENDING);
        when(repository.findById(any())).thenReturn(Optional.of(b));
        assertThrows(RuntimeException.class,
                () -> service.startPayment(UUID.randomUUID(), "XYZ"));
    }



    @Test
    void testCompletePayment_NotFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.completePayment(UUID.randomUUID(), UUID.randomUUID(), "TXN", "PAID"));
    }

    @Test
    void testGetUpcomingBookings() {
        Booking b = new Booking();
        b.setStatus(BookingStatus.CONFIRMED);
        when(repository.findByUserId(1L)).thenReturn(List.of(b));
        List<BookingResponseDTO> result = service.getUpcomingBookings(1L);
        assertEquals(1, result.size());
    }

}