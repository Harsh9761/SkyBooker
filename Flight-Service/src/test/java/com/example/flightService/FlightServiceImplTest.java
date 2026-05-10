package com.example.flightService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import com.example.flightService.dto.FlightDTO;
import com.example.flightService.entity.*;
import com.example.flightService.repository.FlightRepository;
import com.example.flightService.service.FlightServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlightServiceImplTest {

    @Mock
    FlightRepository repository;

    @InjectMocks
    FlightServiceImpl service;

    @Test
    void testAddFlight() {
        FlightDTO dto = new FlightDTO();
        Flight f = new Flight();
        f.setFlightId(1L);
        when(repository.save(any())).thenReturn(f);
        FlightDTO result = service.addFlight(dto);
        assertNotNull(result);
    }

    @Test
    void testGetFlightById() {
        Flight f = new Flight();
        f.setFlightId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(f));
        FlightDTO result = service.getFlightById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetFlightById_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getFlightById(1L));
    }

    @Test
    void testGetFlightByNumber() {
        Flight f = new Flight();
        f.setFlightNumber("AI101");
        when(repository.findByFlightNumber("AI101")).thenReturn(Optional.of(f));
        FlightDTO result = service.getFlightByNumber("AI101");
        assertEquals("AI101", result.getFlightNumber());
    }

    @Test
    void testSearchFlights() {
        Flight f = new Flight();
        when(repository.searchFlights(any(), any(), any(), any())).thenReturn(List.of(f));
        List<FlightDTO> result = service.searchFlights("A", "B", LocalDate.now());
        assertEquals(1, result.size());
    }

    @Test
    void testRoundTrip() {
        when(repository.searchFlights(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        Map<String, List<FlightDTO>> result = service.searchRoundTrip("A", "B", LocalDate.now(), LocalDate.now());
        assertTrue(result.containsKey("onward"));
    }

    @Test
    void testUpdateFlight() {
        Flight f = new Flight();
        when(repository.findById(1L)).thenReturn(Optional.of(f));
        when(repository.save(any())).thenReturn(f);
        FlightDTO result = service.updateFlight(1L, new FlightDTO());
        assertNotNull(result);
    }


    @Test
    void testDecrementSeats() {
        Flight f = new Flight();
        f.setAvailableSeats(10);
        when(repository.findById(1L)).thenReturn(Optional.of(f));
        when(repository.save(any())).thenReturn(f);
        service.decrementSeats(1L, 2);
        assertEquals(8, f.getAvailableSeats());
    }

    @Test
    void testIncrementSeats() {
        Flight f = new Flight();
        f.setAvailableSeats(5);
        f.setTotalSeats(10);
        when(repository.findById(1L)).thenReturn(Optional.of(f));
        when(repository.save(any())).thenReturn(f);
        service.incrementSeats(1L, 2);
        assertEquals(7, f.getAvailableSeats());
    }

    @Test
    void testDeleteFlight() {
        doNothing().when(repository).deleteById(1L);
        service.deleteFlight(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void testGetFlightsByAirline() {
        Flight f = new Flight();
        when(repository.findByAirlineId(1L)).thenReturn(List.of(f));
        List<FlightDTO> result = service.getFlightsByAirline(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllFlights() {
        when(repository.findAll()).thenReturn(List.of(new Flight()));
        List<FlightDTO> result = service.getAllFlights();
        assertEquals(1, result.size());
    }

    @Test
    void testNotEnoughSeats() {
        Flight f = new Flight();
        f.setAvailableSeats(1);
        when(repository.findById(1L)).thenReturn(Optional.of(f));
        assertThrows(RuntimeException.class, () -> service.decrementSeats(1L, 5));
    }

    @Test
    void testSeatLimitExceeded() {
        Flight f = new Flight();
        f.setAvailableSeats(9);
        f.setTotalSeats(10);
        when(repository.findById(1L)).thenReturn(Optional.of(f));
        assertThrows(RuntimeException.class, () -> service.incrementSeats(1L, 5));
    }

    @Test
    void testUpdateNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateFlight(1L, new FlightDTO()));
    }



    @Test
    void testSearchEmpty() {
        when(repository.searchFlights(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        List<FlightDTO> result = service.searchFlights("A", "B", LocalDate.now());
        assertEquals(0, result.size());
    }

    @Test
    void testRoundTripEmpty() {
        when(repository.searchFlights(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        Map<String, List<FlightDTO>> result = service.searchRoundTrip("A", "B", LocalDate.now(), LocalDate.now());
        assertEquals(0, result.get("onward").size());
    }

    @Test
    void testDeleteVerify() {
        doNothing().when(repository).deleteById(1L);
        service.deleteFlight(1L);
        verify(repository).deleteById(1L);
    }
}