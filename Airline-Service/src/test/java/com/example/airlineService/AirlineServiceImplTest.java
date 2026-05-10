package com.example.airlineService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.example.airlineService.dto.*;
import com.example.airlineService.entity.*;
import com.example.airlineService.repository.*;
import com.example.airlineService.service.AirlineServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AirlineServiceImplTest {

    @Mock
    AirlineRepository airlineRepo;

    @Mock
    AirportRepository airportRepo;

    @InjectMocks
    AirlineServiceImpl service;

    @Test
    void testCreateAirline() {
        AirlineDTO dto = new AirlineDTO();
        dto.name = "Indigo";
        dto.iataCode = "6E";

        when(airlineRepo.save(any())).thenAnswer(i -> {
            Airline a = i.getArgument(0);
            a.setAirlineId(1L);
            return a;
        });

        AirlineDTO result = service.createAirline(dto);
        assertEquals(1L, result.airlineId);
    }

    @Test
    void testGetAirlineById() {
        Airline a = new Airline();
        a.setAirlineId(1L);
        when(airlineRepo.findById(1L)).thenReturn(Optional.of(a));

        AirlineDTO result = service.getAirlineById(1L);
        assertEquals(1L, result.airlineId);
    }

    @Test
    void testGetAirlineByIata() {
        Airline a = new Airline();
        a.setAirlineId(1L);
        when(airlineRepo.findByIataCode("6E")).thenReturn(Optional.of(a));

        AirlineDTO result = service.getAirlineByIata("6E");
        assertNotNull(result);
    }

    @Test
    void testGetAllAirlines() {
        Airline a = new Airline();
        when(airlineRepo.findAll()).thenReturn(List.of(a));

        List<AirlineDTO> result = service.getAllAirlines();
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateAirline() {
        Airline a = new Airline();
        when(airlineRepo.findById(1L)).thenReturn(Optional.of(a));
        when(airlineRepo.save(any())).thenReturn(a);

        AirlineDTO dto = new AirlineDTO();
        dto.name = "Updated";

        AirlineDTO result = service.updateAirline(1L, dto);
        assertEquals("Updated", result.name);
    }

    @Test
    void testDeactivateAirline() {
        Airline a = new Airline();
        when(airlineRepo.findById(1L)).thenReturn(Optional.of(a));

        service.deactivateAirline(1L);
        assertFalse(a.isActive());
    }

    @Test
    void testCreateAirport() {
        AirportDTO dto = new AirportDTO();
        dto.name = "Delhi Airport";

        when(airportRepo.save(any())).thenAnswer(i -> {
            Airport a = i.getArgument(0);
            a.setAirportId(10L);
            return a;
        });

        AirportDTO result = service.createAirport(dto);
        assertEquals(10L, result.airportId);
    }

    @Test
    void testGetAirportByIata() {
        Airport a = new Airport();
        a.setAirportId(1L);

        when(airportRepo.findByIataCode("DEL")).thenReturn(Optional.of(a));

        AirportDTO result = service.getAirportByIata("DEL");
        assertNotNull(result);
    }

    @Test
    void testSearchAirports() {
        Airport a = new Airport();
        when(airportRepo.findByCityContainingIgnoreCase("Del"))
                .thenReturn(List.of(a));

        List<AirportDTO> result = service.searchAirports("Del");
        assertEquals(1, result.size());
    }

    @Test
    void testGetAirportsByCity() {
        Airport a = new Airport();
        when(airportRepo.findByCity("Delhi")).thenReturn(List.of(a));

        List<AirportDTO> result = service.getAirportsByCity("Delhi");
        assertEquals(1, result.size());
    }

    @Test
    void testGetAirportsByCountry() {
        Airport a = new Airport();
        when(airportRepo.findByCountry("India")).thenReturn(List.of(a));

        List<AirportDTO> result = service.getAirportsByCountry("India");
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateAirport() {
        Airport a = new Airport();
        when(airportRepo.findById(1L)).thenReturn(Optional.of(a));
        when(airportRepo.save(any())).thenReturn(a);

        AirportDTO dto = new AirportDTO();
        dto.city = "Mumbai";

        AirportDTO result = service.updateAirport(1L, dto);
        assertEquals("Mumbai", result.city);
    }

    @Test
    void testGetAirlineById_NotFound() {
        when(airlineRepo.findById(any())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> service.getAirlineById(99L));
    }

    @Test
    void testGetAirportByIata_NotFound() {
        when(airportRepo.findByIataCode(any())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> service.getAirportByIata("XYZ"));
    }

    @Test
    void testUpdateAirline_NotFound() {
        when(airlineRepo.findById(any())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> service.updateAirline(1L, new AirlineDTO()));
    }

    @Test
    void testDeactivateAirline_NotFound() {
        when(airlineRepo.findById(any())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> service.deactivateAirline(1L));
    }

    @Test
    void testUpdateAirport_NotFound() {
        when(airportRepo.findById(any())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> service.updateAirport(1L, new AirportDTO()));
    }

    @Test
    void testGetAllAirlines_Empty() {
        when(airlineRepo.findAll()).thenReturn(Collections.emptyList());
        List<AirlineDTO> result = service.getAllAirlines();
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchAirports_Empty() {
        when(airportRepo.findByCityContainingIgnoreCase(any()))
                .thenReturn(Collections.emptyList());

        List<AirportDTO> result = service.searchAirports("xyz");
        assertTrue(result.isEmpty());
    }
}