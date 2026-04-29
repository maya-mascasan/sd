package com.andrei.demo.service;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Code");
        message.setText("Your password reset code is: " + code +
                "\nThis code will expire in 15 minutes.");
        mailSender.send(message);
    }

    public void sendPasswordChangedConfirmation(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Security Alert: Password Changed");
        message.setText("Your password was recently updated. " +
                "If you did not make this change, please contact us immediately.");
        mailSender.send(message);
    }
}