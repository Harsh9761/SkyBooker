package com.example.paymentService.service;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Service;

import com.example.paymentService.dto.*;
import com.example.paymentService.entity.*;
import com.example.paymentService.repository.PaymentRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

    private PaymentRepository repository;

    public PaymentServiceImpl(PaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaymentResponseDTO initiatePayment(PaymentRequestDTO request) {

        Payment payment = new Payment();

        payment.setPaymentId(UUID.randomUUID());

        payment.setBookingId(request.getBookingId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());

        payment.setPaymentMode(
                PaymentMode.valueOf(request.getPaymentMode().toUpperCase())
        );

        payment.setStatus(PaymentStatus.PENDING);

        repository.save(payment);

        return mapToDTO(payment);
    }

    @Override
    public PaymentResponseDTO processPayment(UUID paymentId, String transactionId, String status) {

        Payment payment = repository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
        payment.setPaidAt(LocalDateTime.now());

        repository.save(payment);

        return mapToDTO(payment);
    }

    @Override
    public List<PaymentResponseDTO> getPaymentByBooking(Long bookingId) {
        return mapList(repository.findByBookingId(bookingId));
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByUser(Long userId) {
        return mapList(repository.findByUserId(userId));
    }

    @Override
    public PaymentResponseDTO refundPayment(UUID paymentId) {

        Payment payment = repository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

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