package com.example.bookingService.dto;

import java.util.List;
import java.util.UUID;

public class BookingResponseDTO {

    private UUID bookingId;
    private String pnrCode;
    private String status;
    private Double totalFare;
    private List<String> seatNumbers;
    private String contactEmail;
    private String contactPhone;
    private UUID paymentId;

    public BookingResponseDTO() {
    }

    public BookingResponseDTO(UUID bookingId, String pnrCode, String status, Double totalFare,String contactEmail,String contactPhone,UUID paymentId) {
        this.bookingId = bookingId;
        this.pnrCode = pnrCode;
        this.status = status;
        this.totalFare = totalFare;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.paymentId = paymentId;
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

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public UUID getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(UUID paymentId) {
		this.paymentId = paymentId;
	}

	public List<String> getSeatNumbers() {
		return seatNumbers;
	}

	public void setSeatNumbers(List<String> seatNumbers) {
		this.seatNumbers = seatNumbers;
	}

	
}