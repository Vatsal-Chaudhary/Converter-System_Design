package com.vat.notificationservice;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String text) {
        if (!EmailValidator.getInstance().isValid(to)) {
            throw new IllegalArgumentException("Invalid email address: " + to);
        }

        MimeMessage message = mailSender.createMimeMessage();
        try {
            System.out.println("Sending email to: " + to);
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(message);
            System.out.println("Email send successfully");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

