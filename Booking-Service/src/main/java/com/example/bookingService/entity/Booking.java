package com.example.bookingService.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue
    private UUID bookingId;

    private Long userId;
    private Long flightId;

    @Column(unique = true, length = 6)
    private String pnrCode;

    @Enumerated(EnumType.STRING)
    private TripType tripType;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Double totalFare;
    private Double baseFare;
    private Double taxes;

    private String mealPreference;
    private Integer luggageKg;

    private String contactEmail;
    private String contactPhone;

    private LocalDateTime bookedAt;

    private String paymentId;
    private String seatNumber;

    //Constructors

    public Booking() {
    }

    public Booking(UUID bookingId, Long userId, Long flightId, String pnrCode, TripType tripType,
                   BookingStatus status, Double totalFare, Double baseFare, Double taxes,
                   String mealPreference, Integer luggageKg, String contactEmail,
                   String contactPhone, LocalDateTime bookedAt, String paymentId) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.flightId = flightId;
        this.pnrCode = pnrCode;
        this.tripType = tripType;
        this.status = status;
        this.totalFare = totalFare;
        this.baseFare = baseFare;
        this.taxes = taxes;
        this.mealPreference = mealPreference;
        this.luggageKg = luggageKg;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.bookedAt = bookedAt;
        this.paymentId = paymentId;
    }

    //Getters & Setters

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public String getPnrCode() {
        return pnrCode;
    }

    public void setPnrCode(String pnrCode) {
        this.pnrCode = pnrCode;
    }

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(Double totalFare) {
        this.totalFare = totalFare;
    }

    public Double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(Double baseFare) {
        this.baseFare = baseFare;
    }

    public Double getTaxes() {
        return taxes;
    }

    public void setTaxes(Double taxes) {
        this.taxes = taxes;
    }

    public String getMealPreference() {
        return mealPreference;
    }

    public void setMealPreference(String mealPreference) {
        this.mealPreference = mealPreference;
    }

    public Integer getLuggageKg() {
        return luggageKg;
    }

    public void setLuggageKg(Integer luggageKg) {
        this.luggageKg = luggageKg;
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

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

	public String getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(String seatNumber) {
		this.seatNumber = seatNumber;
	}
}