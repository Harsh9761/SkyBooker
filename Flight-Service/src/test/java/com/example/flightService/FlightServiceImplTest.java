package com.example.flightService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.example.flightService.dto.FlightDTO;
import com.example.flightService.entity.Flight;
import com.example.flightService.entity.FlightStatus;
import com.example.flightService.repository.FlightRepository;
import com.example.flightService.service.FlightServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightServiceImpl flightService;

    private Flight flight;
    private FlightDTO dto;

    @BeforeEach
    void setUp() {
        flight = new Flight();
        flight.setFlightId(1L);
        flight.setFlightNumber("AI101");
        flight.setAirlineId(10L);
        flight.setOriginAirportCode("DEL");
        flight.setDestinationAirportCode("BOM");
        flight.setDepartureTime(LocalDateTime.now());
        flight.setArrivalTime(LocalDateTime.now().plusHours(2));
        flight.setDurationMinutes(120);
        flight.setStatus(FlightStatus.ON_TIME);
        flight.setTotalSeats(100);
        flight.setAvailableSeats(80);
        flight.setBasePrice(5000.0);

        dto = new FlightDTO();
        dto.setFlightNumber("AI101");
        dto.setAirlineId(10L);
        dto.setOriginAirportCode("DEL");
        dto.setDestinationAirportCode("BOM");
        dto.setDepartureTime(LocalDateTime.now());
        dto.setArrivalTime(LocalDateTime.now().plusHours(2));
        dto.setDurationMinutes(120);
        dto.setStatus(FlightStatus.ON_TIME);
        dto.setTotalSeats(100);
        dto.setAvailableSeats(80);
        dto.setBasePrice(5000.0);
    }

    //ADD FLIGHT
    @Test
    void testAddFlight() {
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        FlightDTO result = flightService.addFlight(dto);

        assertNotNull(result);
        assertEquals("AI101", result.getFlightNumber());

        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    //GET BY ID
    @Test
    void testGetFlightById() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        FlightDTO result = flightService.getFlightById(1L);

        assertEquals(1L, result.getId());
        assertEquals("AI101", result.getFlightNumber());
    }

    //NOT FOUND
    @Test
    void testGetFlightById_NotFound() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> flightService.getFlightById(1L));
    }

    //UPDATE FLIGHT
    @Test
    void testUpdateFlight() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        FlightDTO result = flightService.updateFlight(1L, dto);

        assertNotNull(result);
        verify(flightRepository).save(any(Flight.class));
    }

    //DELETE
    @Test
    void testDeleteFlight() {
        doNothing().when(flightRepository).deleteById(1L);

        flightService.deleteFlight(1L);

        verify(flightRepository, times(1)).deleteById(1L);
    }

    //DECREMENT SEATS
    @Test
    void testDecrementSeats() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any())).thenReturn(flight);

        flightService.decrementSeats(1L, 5);

        assertEquals(75, flight.getAvailableSeats());
        verify(flightRepository).save(any());
    }

    // ❌ NOT ENOUGH SEATS
    @Test
    void testDecrementSeats_NotEnough() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        assertThrows(RuntimeException.class,
                () -> flightService.decrementSeats(1L, 200));
    }

    //INCREMENT SEATS
    @Test
    void testIncrementSeats() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any())).thenReturn(flight);

        flightService.incrementSeats(1L, 10);

        assertEquals(90, flight.getAvailableSeats());
    }

    //LIMIT EXCEEDED
    @Test
    void testIncrementSeats_Exceeded() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        assertThrows(RuntimeException.class,
                () -> flightService.incrementSeats(1L, 50));
    }

    //SEARCH FLIGHTS
    @Test
    void testSearchFlights() {
        when(flightRepository.searchFlights(
                anyString(), anyString(), any(), any()
        )).thenReturn(List.of(flight));

        List<FlightDTO> result =
                flightService.searchFlights("DEL", "BOM", LocalDate.now());

        assertFalse(result.isEmpty());
    }

    //ROUND TRIP
    @Test
    void testRoundTrip() {
        when(flightRepository.searchFlights(
                any(), any(), any(), any()
        )).thenReturn(List.of(flight));

        Map<String, List<FlightDTO>> result =
                flightService.searchRoundTrip(
                        "DEL", "BOM",
                        LocalDate.now(),
                        LocalDate.now().plusDays(1)
                );

        assertTrue(result.containsKey("onward"));
        assertTrue(result.containsKey("return"));
    }

    //GET BY AIRLINE
    @Test
    void testGetByAirline() {
        when(flightRepository.findByAirlineId(10L))
                .thenReturn(List.of(flight));

        List<FlightDTO> result = flightService.getFlightsByAirline(10L);

        assertEquals(1, result.size());
    }

    //GET ALL
    @Test
    void testGetAllFlights() {
        when(flightRepository.findAll()).thenReturn(List.of(flight));

        List<FlightDTO> result = flightService.getAllFlights();

        assertEquals(1, result.size());
    }
}