package com.example.notificationService.service;

import com.example.notificationService.entity.*;
import com.example.notificationService.repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

//@Override
    public void sendBookingConfirmation(Long userId, String bookingId, String email, String phone) {

        //APP Notification save
        Notification appNotif = new Notification();
        appNotif.setRecipientId(userId);
        appNotif.setType(NotificationType.BOOKING_CONFIRMED);
        appNotif.setChannel(Channel.APP);
        appNotif.setTitle("Booking Confirmed");
        appNotif.setMessage("Your booking is confirmed. PNR: " + bookingId);
        appNotif.setRelatedBookingId(bookingId);
        appNotif.setSentAt(LocalDateTime.now());
        appNotif.setIsRead(false);

        repo.save(appNotif);

        //  EMAIL Notification
        String emailBody = "Booking Confirmed\n\n" +
                "Your booking is successfully confirmed.\n" +
                "PNR: " + bookingId + "\n\n" +
                "Thank you for choosing us.";

        sendEmail(email, "Booking Confirmation", emailBody);

        //SMS Notification
        String smsMessage = "Booking confirmed. PNR: " + bookingId;
        sendSMS(phone, smsMessage);
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

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String email, String subject, String body) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }

    public void sendSMS(String phone, String message) {
        System.out.println("SMS sent to " + phone + " | " + message);
    }
    
    
    
}