package com.example.notification_service.dto;

import com.example.notification_service.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private String recipient;
    private String subject;
    private NotificationType type;
    private Map<String, Object> templateVariables; // e.g., {"userName": "John", "amount": "150.00"}

}