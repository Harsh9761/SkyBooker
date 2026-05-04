package com.example.bookingService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    BookingRepository bookingRepository;

    @Mock
    PaymentClient paymentClient;

    @Mock
    SeatClient seatClient;

    @Mock
    FlightClient flightClient;

    @InjectMocks
    BookingServiceImpl bookingService;

    @Test
    void testGetBookingById() {

        UUID id = UUID.randomUUID();

        Booking booking = new Booking();
        booking.setBookingId(id);
        booking.setPnrCode("ABC123");
        booking.setTotalFare(5000.0);
        booking.setSeatNumber("12A");
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(booking));

        BookingResponseDTO result = bookingService.getBookingById(id);

        assertNotNull(result);
        assertEquals("ABC123", result.getPnrCode());
    }

    @Test
    void testGetBookingsByUser() {

        Long userId = 1L;

        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        booking.setUserId(userId);
        booking.setPnrCode("PNR123");
        booking.setSeatNumber("12A");
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalFare(5000.0);

        when(bookingRepository.findByUserId(userId))
                .thenReturn(List.of(booking));

        List<BookingResponseDTO> result =
                bookingService.getBookingsByUser(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PNR123", result.get(0).getPnrCode());

        verify(bookingRepository).findByUserId(userId);
    }

    @Test
    void testCreateBooking_SuccessFlow() {

        BookingRequestDTO req = new BookingRequestDTO();
        req.setUserId(1L);
        req.setFlightId(101L);
        req.setSeatNumber("12A");
        req.setTripType("ONE_WAY");
        req.setLuggageKg(10);

        when(bookingRepository.save(any())).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setBookingId(UUID.randomUUID());
            return b;
        });

        BookingResponseDTO result = bookingService.createBooking(req);

        assertNotNull(result);
    }
}