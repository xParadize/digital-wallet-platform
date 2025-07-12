package com.wallet.notificationservice.service;

import com.wallet.notificationservice.event.CardBlockedEvent;
import com.wallet.notificationservice.event.CardFrozenEvent;
import com.wallet.notificationservice.event.CardLinkedEvent;
import com.wallet.notificationservice.event.CardUnfrozenEvent;
import freemarker.template.Configuration;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final Configuration configuration;
    private final JavaMailSender mailSender;
    private StringWriter writer;

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

    @Async
    @SneakyThrows
    public void sendEmailConfirmed(String email) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("Email confirmed");
        helper.setTo(email);
        String emailContent = getEmailConfirmed();
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getEmailConfirmed() {
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        configuration.getTemplate("email_confirmed.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }

    @Async
    @SneakyThrows
    public void sendPasswordChanged(String email) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("Password changed");
        helper.setTo(email);
        String emailContent = getPasswordChanged();
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getPasswordChanged() {
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        configuration.getTemplate("password_changed.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }

    @Async
    @SneakyThrows
    public void sendCardLinked(CardLinkedEvent event) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("Card linked successfully");
        helper.setTo(event.email());
        String emailContent = getCardLinked(event);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getCardLinked(CardLinkedEvent event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("card_number", event.cardNumber());
        model.put("issuer", event.cardIssuer());
        model.put("scheme", event.cardScheme());
        model.put("linked_at", event.linkedAt().format(formatter));
        configuration.getTemplate("card_linked.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }

    @Async
    @SneakyThrows
    public void sendCardFrozen(CardFrozenEvent event) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("Card frozen successfully");
        helper.setTo(event.email());
        String emailContent = getCardFrozen(event);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getCardFrozen(CardFrozenEvent event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("card_number", event.cardNumber());
        model.put("frozen_at", event.frozenAt().format(formatter));
        configuration.getTemplate("card_frozen.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }

    @Async
    @SneakyThrows
    public void sendCardUnfrozen(CardUnfrozenEvent event) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("Card unfrozen successfully");
        helper.setTo(event.email());
        String emailContent = getCardUnfrozen(event);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getCardUnfrozen(CardUnfrozenEvent event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("card_number", event.cardNumber());
        model.put("unfrozen_at", event.unfrozenAt().format(formatter));
        configuration.getTemplate("card_unfrozen.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }

    @Async
    @SneakyThrows
    public void sendCardBlocked(CardBlockedEvent event) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("Card blocked successfully");
        helper.setTo(event.email());
        String emailContent = getCardBlocked(event);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getCardBlocked(CardBlockedEvent event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("card_number", event.cardNumber());
        model.put("blocked_at", event.blockedAt().format(formatter));
        configuration.getTemplate("card_blocked.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }
}