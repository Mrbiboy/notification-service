package com.example.notification_service.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String recipient;
    private String subject;
    private String body;
    @Enumerated(EnumType.STRING)  // Store as string in DB
    private NotificationType type;  // New field
    private String status;
    private LocalDateTime sentAt;
    @Lob  // Allows large text (no 255 char limit)
    @Column(name = "template_variables")
    private String templateVariables;

}