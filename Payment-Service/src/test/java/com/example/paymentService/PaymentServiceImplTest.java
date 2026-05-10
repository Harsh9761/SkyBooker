package com.example.paymentService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.example.paymentService.client.BookingClient;
import com.example.paymentService.dto.*;
import com.example.paymentService.entity.*;
import com.example.paymentService.repository.PaymentRepository;
import com.example.paymentService.service.PaymentServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    PaymentRepository repository;

    @Mock
    BookingClient bookingClient;

    @InjectMocks
    PaymentServiceImpl paymentService;
  

    // ----------------  PROCESS PAYMENT NOT FOUND ----------------
    @Test
    void testProcessPayment_NotFound() {

        UUID id = UUID.randomUUID();

        when(repository.findByPaymentId(id))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.processPayment(id, "TXN", "PAID"));

        assertEquals("Payment not found", ex.getMessage());
    }

    
    @Test
    void testRefundPayment_AlreadyRefunded() {

        UUID id = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setPaymentId(id);
        payment.setStatus(PaymentStatus.REFUNDED);

        when(repository.findByPaymentId(id))
                .thenReturn(Optional.of(payment));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.refundPayment(id));

        assertEquals("Already refunded", ex.getMessage());
    }

    @Test
    void testGetPaymentStatus() {

        UUID id = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setPaymentId(id);
        payment.setStatus(PaymentStatus.PAID);

        when(repository.findByPaymentId(id))
                .thenReturn(Optional.of(payment));

        String status = paymentService.getPaymentStatus(id);

        assertEquals("PAID", status);
    }

   

    @Test
    void testGenerateReceipt() {

        UUID id = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setPaymentId(id);
        payment.setAmount(200.0);
        payment.setStatus(PaymentStatus.PAID);

        when(repository.findByPaymentId(id))
                .thenReturn(Optional.of(payment));

        String receipt = paymentService.generateReceipt(id);

        assertTrue(receipt.contains("Receipt"));
        assertTrue(receipt.contains("200.0"));
    }


    @Test
    void testGetRevenue() {

        Payment p1 = new Payment();
        p1.setAmount(100.0);
        p1.setStatus(PaymentStatus.PAID);

        Payment p2 = new Payment();
        p2.setAmount(200.0);
        p2.setStatus(PaymentStatus.PAID);

        when(repository.findByStatus(PaymentStatus.PAID))
                .thenReturn(List.of(p1, p2));

        Double revenue = paymentService.getRevenue();

        assertEquals(300.0, revenue);
    }


    @Test
    void testUpdatePaymentStatus_NotFound() {

        UUID id = UUID.randomUUID();

        when(repository.findByPaymentId(id))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.updatePaymentStatus(id, "PAID"));

        assertEquals("Payment not found", ex.getMessage());
    }


    @Test
    void testGenerateReceipt_NotFound() {

        UUID id = UUID.randomUUID();

        when(repository.findByPaymentId(id))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.generateReceipt(id));

        assertEquals("Payment not found", ex.getMessage());
    }


    @Test
    void testGetRevenue_Empty() {

        when(repository.findByStatus(PaymentStatus.PAID))
                .thenReturn(Collections.emptyList());

        Double revenue = paymentService.getRevenue();

        assertEquals(0.0, revenue);
    }


    @Test
    void testRefund_NotFound() {

        UUID id = UUID.randomUUID();

        when(repository.findByPaymentId(id))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.refundPayment(id));

        assertEquals("Payment not found", ex.getMessage());
    }
    
    
}