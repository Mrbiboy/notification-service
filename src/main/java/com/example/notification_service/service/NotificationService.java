package com.example.notification_service.service;

import com.example.notification_service.entity.Notification;
import com.example.notification_service.entity.NotificationType;
import com.example.notification_service.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private ObjectMapper objectMapper;  // For converting Map to JSON string

    /**
     * Sends an HTML email using a Thymeleaf template based on the notification type.
     * Stores only the template variables (as JSON) in the database for traceability.
     *
     * @param recipient     Email address of the recipient
     * @param subject       Email subject
     * @param templateVars  Map of variables to pass to the template (e.g., userName, amount, etc.)
     * @param type          Type of notification to choose the correct template
     */
    public void sendTemplatedEmail(String recipient,
                                   String subject,
                                   Map<String, Object> templateVars,
                                   NotificationType type) {

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setType(type);
        notification.setSentAt(LocalDateTime.now());

        String templateName;
        switch (type) {
            case REGISTRATION:
                templateName = "registration";
                break;
            case TRANSACTION_SUCCESS:
                templateName = "transaction_success";
                break;
            default:
                templateName = "generic";
                break;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipient);
            helper.setSubject(subject);


            Context context = new Context();
            if (templateVars != null) {
                templateVars.forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process(templateName, context);

            helper.setText(htmlContent, true); // true = isHtml

            mailSender.send(message);

            notification.setStatus("SENT");

            // Store ONLY the template variables as JSON string
            String varsJson = objectMapper.writeValueAsString(templateVars != null ? templateVars : Map.of());
            notification.setTemplateVariables(varsJson);

            // We no longer store the full rendered HTML to avoid large text issues
            // notification.setBody(htmlContent);  // Commented out intentionally

        } catch (MailException | MessagingException e) {
            notification.setStatus("FAILED: " + e.getMessage());

            // Still store variables even on failure for debugging
            if (templateVars != null) {
                String varsJson = objectMapper.writeValueAsString(templateVars);
                notification.setTemplateVariables(varsJson);
            }

            throw new RuntimeException("Failed to send email", e);
        } finally {
            notificationRepository.save(notification);
        }
    }
    // Optional: Keep old method for backward compatibility or simple plain-text emails
    public void sendPlainEmail(String recipient, String subject, String body, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setType(type);
        notification.setSentAt(LocalDateTime.now());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            notification.setStatus("SENT");
        } catch (MailException e) {
            notification.setStatus("FAILED: " + e.getMessage());
            throw e;
        } finally {
            notificationRepository.save(notification);
        }
    }
}