package com.example.paymentService.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonProperty;
public class PaymentRequestDTO {

//	@NotNull(message = "Booking ID is required")
	@JsonProperty("bookingId")
    private UUID bookingId;

//    @NotNull(message = "User ID is required")
	
	@JsonProperty("userId")
    private Long userId;

//    @NotNull(message = "Amount is required")
//    @Positive(message = "Amount must be greater than 0")
    
    @JsonProperty("amount")
    private Double amount;

//    @NotBlank(message = "Currency is required")
    
    @JsonProperty("currency")
    private String currency;

//    @NotBlank(message = "Payment mode is required")
//    @Pattern(
//        regexp = "CARD|UPI|NETBANKING|WALLET",
//        message = "Invalid payment mode"
//    )
    
    @JsonProperty("paymentMode")
    private String paymentMode;
    
    @Override
    public String toString() {
        return "PaymentRequestDTO{" +
                "bookingId=" + bookingId +
                ", userId=" + userId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", paymentMode='" + paymentMode + '\'' +
                '}';
    }

    public PaymentRequestDTO() {}

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
}