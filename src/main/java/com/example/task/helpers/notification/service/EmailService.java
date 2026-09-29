package com.example.task.helpers.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private JavaMailSender mailSender;

    @Async
    public void sendMail(String to, String subject, String body){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@taskapp.com");
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e){
            log.warn("Failed to send email to {}", to, e);
        }
    }
}
