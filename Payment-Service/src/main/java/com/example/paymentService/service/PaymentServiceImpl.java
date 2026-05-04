package com.example.paymentService.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.paymentService.client.BookingClient;
import com.example.paymentService.dto.*;
import com.example.paymentService.entity.*;
import com.example.paymentService.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class PaymentServiceImpl implements PaymentService {

    private PaymentRepository repository;
    private BookingClient bookingClient;

    public PaymentServiceImpl(PaymentRepository repository,BookingClient bookingClient) {
        this.repository = repository;
        this.bookingClient =bookingClient;
    }
    
    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    @Override
    public PaymentResponseDTO initiatePayment(PaymentRequestDTO request) {

        try {

            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject options = new JSONObject();
            options.put("amount", (int) Math.round(request.getAmount() * 100));
            options.put("currency", request.getCurrency());
            
            String receiptId = "txn_" + System.currentTimeMillis();
            options.put("receipt", receiptId);

            Order order = client.orders.create(options);

            Payment payment = new Payment();
            payment.setBookingId(request.getBookingId());
            payment.setUserId(request.getUserId());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency().trim().toUpperCase());

            // SAFE ENUM HANDLING
            payment.setPaymentMode(
                PaymentMode.valueOf(request.getPaymentMode().trim().toUpperCase())
            );

            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(order.get("id"));

            repository.save(payment);

            return mapToDTO(payment);

        } catch (Exception e) {
            throw new RuntimeException("Payment initiation failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDTO processPayment(UUID paymentId, String transactionId, String status) {

        Payment payment = repository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setTransactionId(transactionId);

        PaymentStatus ps = PaymentStatus.valueOf(status.toUpperCase());
        payment.setStatus(ps);

        payment.setPaidAt(LocalDateTime.now());

        repository.save(payment);

        if (ps == PaymentStatus.PAID || ps == PaymentStatus.FAILED) {

        	try {
        	    System.out.println(" CALLING BOOKING CALLBACK");

        	    bookingClient.callback(
        	        payment.getBookingId(),
        	        payment.getPaymentId(),
        	        transactionId,
        	        ps.name()
        	    );

        	    System.out.println("CALLBACK SUCCESS");

        	} catch (Exception e) {
        	    System.out.println(" CALLBACK FAILED");
        	    e.printStackTrace(); //  IMPORTANT
        	}
        }

        return mapToDTO(payment);
    }

    @Override
    public List<PaymentResponseDTO> getPaymentByBooking(UUID bookingId) {
        return mapList(repository.findByBookingId(bookingId));
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByUser(Long userId) {
        return mapList(repository.findByUserId(userId));
    }

    @Override
    public PaymentResponseDTO refundPayment(UUID paymentId) {
    	System.out.println("hello2");
        Payment payment = repository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new RuntimeException("Already refunded");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(payment.getAmount());
        payment.setRefundedAt(LocalDateTime.now());

        repository.save(payment);

        return mapToDTO(payment);
    }

    @Override
    public String getPaymentStatus(UUID paymentId) {
        return repository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"))
                .getStatus()
                .name();
    }

    @Override
    public PaymentResponseDTO updatePaymentStatus(UUID paymentId, String status) {

        Payment payment = repository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.valueOf(status.toUpperCase()));

        repository.save(payment);

        return mapToDTO(payment);
    }

    @Override
    public String generateReceipt(UUID paymentId) {

        Payment p = repository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return "Receipt\nPaymentId: " + p.getPaymentId() +
                "\nAmount: " + p.getAmount() +
                "\nStatus: " + p.getStatus();
    }

    @Override
    public Double getRevenue() {

        List<Payment> paidPayments = repository.findByStatus(PaymentStatus.PAID);

        double total = 0.0;

        for (Payment p : paidPayments) {
            total += p.getAmount();
        }

        return total;
    }

    //MAPPERS

    private PaymentResponseDTO mapToDTO(Payment p) {

        PaymentResponseDTO dto = new PaymentResponseDTO();

        dto.setPaymentId(p.getPaymentId());
        dto.setBookingId(p.getBookingId());
        dto.setUserId(p.getUserId());
        dto.setAmount(p.getAmount());
        dto.setCurrency(p.getCurrency());
        dto.setStatus(p.getStatus().name());
        dto.setPaymentMode(p.getPaymentMode().name());
        dto.setTransactionId(p.getTransactionId());
        dto.setPaidAt(p.getPaidAt());

        return dto;
    }

    private List<PaymentResponseDTO> mapList(List<Payment> list) {

        List<PaymentResponseDTO> result = new ArrayList<>();

        for (Payment p : list) {
            result.add(mapToDTO(p));
        }

        return result;
    }
}