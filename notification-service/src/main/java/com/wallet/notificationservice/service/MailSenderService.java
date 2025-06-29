package com.wallet.notificationservice.service;

import freemarker.template.Configuration;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final Configuration configuration;
    private final JavaMailSender mailSender;

    @Async
    @SneakyThrows
    public void sendVerifyEmail(String email, String name, UUID userId) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("To continue please confirm your email address");
        helper.setTo(email);
        String emailContent = getVerifyEmail(name, userId);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getVerifyEmail(String name, UUID confirmationCode) {
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("name", name);
        model.put("code", confirmationCode);
        configuration.getTemplate("email_confirmation.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }
}