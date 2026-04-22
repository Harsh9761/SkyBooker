package com.example.bookingService.dto;

import java.util.UUID;

public class BookingResponseDTO {

    private UUID bookingId;
    private String pnrCode;
    private String status;
    private Double totalFare;

    public BookingResponseDTO() {
    }

    public BookingResponseDTO(UUID bookingId, String pnrCode, String status, Double totalFare) {
        this.bookingId = bookingId;
        this.pnrCode = pnrCode;
        this.status = status;
        this.totalFare = totalFare;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public String getPnrCode() {
        return pnrCode;
    }

    public void setPnrCode(String pnrCode) {
        this.pnrCode = pnrCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(Double totalFare) {
        this.totalFare = totalFare;
    }
}