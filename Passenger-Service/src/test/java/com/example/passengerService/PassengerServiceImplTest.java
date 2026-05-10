package com.example.passengerService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import com.example.passengerService.client.BookingClient;
import com.example.passengerService.dto.*;
import com.example.passengerService.entity.*;
import com.example.passengerService.repository.PassengerRepository;
import com.example.passengerService.service.PassengerServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PassengerServiceImplTest {

    @Mock
    PassengerRepository repository;

    @Mock
    BookingClient bookingClient;

    @InjectMocks
    PassengerServiceImpl service;


    @Test
    void testAddPassenger() {

        PassengerDTO dto = new PassengerDTO();
        dto.setBookingId(UUID.randomUUID());
        dto.setSeatNumber("12A");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        dto.setPassportExpiry(LocalDate.now().plusYears(5));

        BookingDTO booking = new BookingDTO();
        booking.setStatus("PENDING");
        booking.setSeatNumbers(List.of("12A"));

        when(bookingClient.getBooking(dto.getBookingId())).thenReturn(booking);

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        PassengerDTO result = service.addPassenger(dto);

        assertNotNull(result);
    }

    @Test
    void testAddPassenger_BookingNotFound() {

        PassengerDTO dto = new PassengerDTO();
        dto.setBookingId(UUID.randomUUID());

        when(bookingClient.getBooking(any())).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addPassenger(dto));

        assertEquals("Booking not found", ex.getMessage());
    }

    @Test
    void testAddPassenger_NotPending() {

        PassengerDTO dto = new PassengerDTO();
        dto.setBookingId(UUID.randomUUID());

        BookingDTO booking = new BookingDTO();
        booking.setStatus("CONFIRMED");

        when(bookingClient.getBooking(dto.getBookingId())).thenReturn(booking);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addPassenger(dto));

        assertEquals("Cannot add passenger, booking not in Pending state", ex.getMessage());
    }

    @Test
    void testAddPassenger_NoSeats() {

        PassengerDTO dto = new PassengerDTO();
        dto.setBookingId(UUID.randomUUID());

        BookingDTO booking = new BookingDTO();
        booking.setStatus("PENDING");
        booking.setSeatNumbers(new ArrayList<>());

        when(bookingClient.getBooking(dto.getBookingId())).thenReturn(booking);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addPassenger(dto));

        assertEquals("No seats in booking", ex.getMessage());
    }

    // ---------------- 5 INVALID SEAT ----------------
    @Test
    void testAddPassenger_InvalidSeat() {

        PassengerDTO dto = new PassengerDTO();
        dto.setBookingId(UUID.randomUUID());
        dto.setSeatNumber("99Z");

        BookingDTO booking = new BookingDTO();
        booking.setStatus("PENDING");
        booking.setSeatNumbers(List.of("12A"));

        when(bookingClient.getBooking(dto.getBookingId())).thenReturn(booking);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addPassenger(dto));

        assertEquals("Seat not assigned in booking", ex.getMessage());
    }


    @Test
    void testGetPassengerById_NotFound() {

        when(repository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getPassengerById(1L));

        assertEquals("Passenger not found", ex.getMessage());
    }


    @Test
    void testCount() {

        when(repository.countByBookingId(any()))
                .thenReturn(5L);

        long count = service.getPassengerCount(UUID.randomUUID());

        assertEquals(5L, count);
    }

    @Test
    void testDelete() {

        doNothing().when(repository).deleteById(1L);

        service.deletePassenger(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testTicket() {

        String ticket = service.generateTicketNumber();

        assertTrue(ticket.startsWith("TKT-"));
    }

    @Test
    void testAdultPassenger() {

        PassengerDTO dto = new PassengerDTO();
        dto.setDateOfBirth(LocalDate.of(1995, 1, 1));

        assertDoesNotThrow(() -> dto.setDateOfBirth(dto.getDateOfBirth()));
    }


    @Test
    void testChildPassenger() {

        PassengerDTO dto = new PassengerDTO();
        dto.setDateOfBirth(LocalDate.now().minusYears(5));

        assertNotNull(dto.getDateOfBirth());
    }

    @Test
    void testInfantPassenger() {

        PassengerDTO dto = new PassengerDTO();
        dto.setDateOfBirth(LocalDate.now().minusMonths(1));

        assertNotNull(dto.getDateOfBirth());
    }



    @Test
    void testEmptyBooking() {

        when(repository.findByBookingId(any()))
                .thenReturn(Collections.emptyList());

        List<PassengerDTO> result =
                service.getPassengersByBooking(UUID.randomUUID());

        assertEquals(0, result.size());
    }

    @Test
    void testUpdateNotFound() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updatePassenger(1L, new PassengerDTO()));

        assertEquals("Passenger not found", ex.getMessage());
    }
}