package com.example.notificationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.example.notificationService.entity.*;
import com.example.notificationService.repository.NotificationRepository;
import com.example.notificationService.service.NotifServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class NotifServiceImplTest {

    @Mock
    NotificationRepository repo;

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    NotifServiceImpl service;

    @Test
    void testSendNotification() {

        Notification n = new Notification();
        n.setTitle("Test");

        when(repo.save(any())).thenReturn(n);

        Notification result = service.send(n);

        assertNotNull(result);
    }

    @Test
    void testMarkAsRead() {

        Notification n = new Notification();
        n.setNotificationId(1L);
        n.setIsRead(false);

        when(repo.findById(1L)).thenReturn(Optional.of(n));
        when(repo.save(any())).thenReturn(n);

        Notification result = service.markAsRead(1L);

        assertTrue(result.getIsRead());
    }

    @Test
    void testMarkAsRead_NotFound() {

        when(repo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.markAsRead(1L));

        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void testGetByRecipient() {

        Notification n = new Notification();
        n.setRecipientId(1L);

        when(repo.findByRecipientId(1L))
                .thenReturn(List.of(n));

        List<Notification> result = service.getByRecipient(1L);

        assertEquals(1, result.size());
    }

    // ---------------- 5 UNREAD COUNT ----------------
    @Test
    void testUnreadCount() {

        when(repo.countByRecipientIdAndIsRead(1L, false))
                .thenReturn(5L);

        long count = service.getUnreadCount(1L);

        assertEquals(5L, count);
    }

    @Test
    void testDeleteNotification() {

        doNothing().when(repo).deleteByNotificationId(1L);

        service.deleteNotification(1L);

        verify(repo, times(1)).deleteByNotificationId(1L);
    }

    @Test
    void testSendBulk() {

        Notification n1 = new Notification();
        Notification n2 = new Notification();

        when(repo.save(any())).thenReturn(n1);

        service.sendBulk(List.of(n1, n2));

        verify(repo, times(2)).save(any());
    }

    @Test
    void testMarkAllRead() {

        Notification n = new Notification();
        n.setIsRead(false);

        when(repo.findByRecipientId(1L))
                .thenReturn(List.of(n));

        when(repo.saveAll(any())).thenReturn(List.of(n));

        service.markAllRead(1L);

        assertTrue(n.getIsRead());
    }

    @Test
    void testSendEmailFailureSafe() {

        assertDoesNotThrow(() ->
                service.sendEmail("test@test.com", "sub", "body"));
    }

    @Test
    void testSendTimestamp() {

        Notification n = new Notification();

        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Notification result = service.send(n);

        assertNotNull(result.getSentAt());
    }

    @Test
    void testEmptyRecipient() {

        when(repo.findByRecipientId(1L))
                .thenReturn(Collections.emptyList());

        List<Notification> result = service.getByRecipient(1L);

        assertEquals(0, result.size());
    }

    @Test
    void testUnreadZero() {

        when(repo.countByRecipientIdAndIsRead(1L, false))
                .thenReturn(0L);

        long count = service.getUnreadCount(1L);

        assertEquals(0L, count);
    }


    @Test
    void testBulkMultiple() {

        Notification n1 = new Notification();
        Notification n2 = new Notification();

        when(repo.save(any())).thenReturn(n1);

        service.sendBulk(List.of(n1, n2, n1));

        verify(repo, times(3)).save(any());
    }


    @Test
    void testDeleteEdge() {

        doNothing().when(repo).deleteByNotificationId(99L);

        service.deleteNotification(99L);

        verify(repo).deleteByNotificationId(99L);
    }


    @Test
    void testMarkAllReadMultiple() {

        Notification n1 = new Notification();
        Notification n2 = new Notification();

        when(repo.findByRecipientId(1L))
                .thenReturn(List.of(n1, n2));

        when(repo.saveAll(any())).thenReturn(List.of(n1, n2));

        service.markAllRead(1L);

        assertTrue(n1.getIsRead());
        assertTrue(n2.getIsRead());
    }


    @Test
    void testSendEmail() {

        assertDoesNotThrow(() ->
                service.sendEmail("a@b.com", "hello", "msg"));
    }


    @Test
    void testSendSMS() {

        assertDoesNotThrow(() ->
                service.sendSMS("9999999999", "hello"));
    }


    @Test
    void testSendNullSafe() {

        Notification n = new Notification();

        when(repo.save(any())).thenReturn(n);

        Notification result = service.send(n);

        assertNotNull(result);
    }

    @Test
    void testRepositorySaveCalled() {

        Notification n = new Notification();

        when(repo.save(any())).thenReturn(n);

        service.send(n);

        verify(repo, times(1)).save(any());
    }


    @Test
    void testDeleteVerify() {

        doNothing().when(repo).deleteByNotificationId(1L);

        service.deleteNotification(1L);

        verify(repo).deleteByNotificationId(1L);
    }
}