package com.example.notificationService.controller;

import com.example.notificationService.dto.BookingNotificationDto;
import com.example.notificationService.entity.Notification;
import com.example.notificationService.repository.NotificationRepository;
import com.example.notificationService.service.NotifService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotifResource {

    @Autowired
    private NotifService service;

    @Autowired
    private NotificationRepository repo;

    @GetMapping("/user/{recipientId}")
    public List<Notification> getByRecipient(@PathVariable Long recipientId) {
        return service.getByRecipient(recipientId);
    }

    @PutMapping("/read/{notificationId}")
    public Notification markAsRead(@PathVariable Long notificationId) {
        return service.markAsRead(notificationId);
    }

    @PutMapping("/read-all/{recipientId}")
    public String markAllRead(@PathVariable Long recipientId) {
        service.markAllRead(recipientId);
        return "All notifications marked as read";
    }

    @GetMapping("/unread-count/{recipientId}")
    public long getUnreadCount(@PathVariable Long recipientId) {
        return service.getUnreadCount(recipientId);
    }

    @DeleteMapping("/{notificationId}")
    public String delete(@PathVariable Long notificationId) {
        service.deleteNotification(notificationId);
        return "Notification deleted";
    }

    @PostMapping("/bulk")
    public String sendBulk(@RequestBody List<Notification> notifications) {
        service.sendBulk(notifications);
        return "Bulk notifications sent";
    }

    @GetMapping("/all")
    public List<Notification> getAll() {
        return repo.findAll();
    }
    
    @PostMapping("/booking-confirmation")
    public Map<String, String> sendBookingConfirmation(@RequestBody BookingNotificationDto dto) {

        service.sendBookingConfirmation(
                dto.getUserId(),
                dto.getBookingId(),
                dto.getEmail(),
                dto.getPhone()
        );

        return Map.of("message", "Booking notification sent");
    }
}