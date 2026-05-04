package com.example.passengerService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.example.passengerService.client.BookingClient;
import com.example.passengerService.dto.BookingDTO;
import com.example.passengerService.dto.PassengerDTO;
import com.example.passengerService.entity.PassengerInfo;
import com.example.passengerService.entity.PassengerType;
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

        UUID bookingId = UUID.randomUUID();

        BookingDTO booking = new BookingDTO();
        booking.setBookingId(bookingId);
        booking.setStatus("PENDING");
        booking.setSeatNumber("12A");

        PassengerDTO dto = new PassengerDTO();
        dto.setBookingId(bookingId);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        dto.setNationality("Indian");
        dto.setPassportExpiry(LocalDate.of(2030, 1, 1));

        PassengerInfo saved = new PassengerInfo();
        saved.setPassengerId(1L);
        saved.setBookingId(bookingId);
        saved.setFirstName("John");
        saved.setLastName("Doe");
        saved.setSeatNumber("12A");
        saved.setPassengerType(PassengerType.ADULT);
        saved.setPassportExpiry("2030-01-01");

        when(bookingClient.getBooking(bookingId)).thenReturn(booking);
        when(repository.save(any())).thenReturn(saved);

        PassengerDTO result = service.addPassenger(dto);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void testGetPassengerById() {

        PassengerInfo p = new PassengerInfo();
        p.setPassengerId(1L);
        p.setFirstName("John");
        p.setLastName("Doe");

        p.setDateOfBirth(LocalDate.of(2000, 1, 1));
        p.setPassportExpiry("2030-01-01");

        // IMPORTANT FIX
        p.setPassengerType(PassengerType.ADULT);

        when(repository.findById(1L)).thenReturn(Optional.of(p));

        PassengerDTO result = service.getPassengerById(1L);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void testDeletePassenger() {

        doNothing().when(repository).deleteById(1L);

        service.deletePassenger(1L);

        verify(repository).deleteById(1L);
    }
}