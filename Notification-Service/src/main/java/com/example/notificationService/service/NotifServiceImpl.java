package com.example.notificationService.service;

import com.example.notificationService.entity.*;
import com.example.notificationService.repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotifServiceImpl implements NotifService {

    @Autowired
    private NotificationRepository repo;

    @Override
    public Notification send(Notification notification) {
        notification.setSentAt(LocalDateTime.now());
        return repo.save(notification);
    }

    @Override
    public void sendBookingConfirmation(Long userId, String bookingId, String email, String phone) {

        Notification appNotif = new Notification();
        appNotif.setRecipientId(userId);
        appNotif.setType(NotificationType.BOOKING_CONFIRMED);
        appNotif.setChannel(Channel.APP);
        appNotif.setTitle("Booking Confirmed");
        appNotif.setMessage("Your booking is confirmed. PNR: " + bookingId);
        appNotif.setRelatedBookingId(bookingId);

        send(appNotif);

        sendEmail(email, "Booking Confirmed", "Your ticket is attached. PNR: " + bookingId);
        sendSMS(phone, "Booking confirmed. PNR: " + bookingId);
    }

    @Override
    public void sendBulk(List<Notification> notifications) {
        for (Notification n : notifications) {
            send(n);
        }
    }

    @Override
    public Notification markAsRead(Long notificationId) {
        Notification notif = repo.findById(notificationId).orElseThrow(() -> new RuntimeException("Not found"));
        notif.setIsRead(true);
        return repo.save(notif);
    }

    @Override
    public void markAllRead(Long recipientId) {
        List<Notification> list = repo.findByRecipientId(recipientId);
        for (Notification n : list) {
            n.setIsRead(true);
        }
        repo.saveAll(list);
    }

    @Override
    public List<Notification> getByRecipient(Long recipientId) {
        return repo.findByRecipientId(recipientId);
    }

    @Override
    public long getUnreadCount(Long recipientId) {
        return repo.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        repo.deleteByNotificationId(notificationId);
    }

    //Helper Methods

    public void sendEmail(String email, String subject, String body) {
        System.out.println("Email sent to " + email + " | " + subject);
    }

    public void sendSMS(String phone, String message) {
        System.out.println("SMS sent to " + phone + " | " + message);
    }
}