package com.example.bookingService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bookingService.dto.FlightResponseDTO;

@FeignClient(name = "flight-service", url = "http://localhost:8082")
public interface FlightClient {

    @GetMapping("/flights/{id}")
    FlightResponseDTO getFlightById(@PathVariable Long id);

    @PutMapping("/flights/{id}/decrement")
    void decrementSeat(@PathVariable Long id);

    @PutMapping("/flights/{id}/increment")
    void incrementSeat(@PathVariable Long id);
}