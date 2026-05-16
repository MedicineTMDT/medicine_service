package com.ryo.identity.service;

import com.ryo.identity.constant.Role;
import com.ryo.identity.entity.User;
import com.ryo.identity.service.impl.EmailService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private EmailService emailService;

    @Test
    void sendOtp_shouldSendSimpleMailMessage() {
        User user = user("patient20@example.com");

        emailService.sendOtp(user, "123456");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(1000)).send(captor.capture());
        assertArrayEquals(new String[]{"patient20@example.com"}, captor.getValue().getTo());
        assertTrue(captor.getValue().getText().contains("123456"));
    }

    @Test
    void sendPasswordChangeEmail_shouldSendSimpleMailMessage() {
        User user = user("patient21@example.com");

        emailService.sendPasswordChangeEmail(user);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(1000)).send(captor.capture());
        assertArrayEquals(new String[]{"patient21@example.com"}, captor.getValue().getTo());
        assertNotNull(captor.getValue().getSubject());
    }

    @Test
    void getRequest_shouldSendRequestEmailToConfiguredAddress() {
        User user = user("patient22@example.com");

        emailService.getRequest(user, "Add drug", "Please add this drug");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(1000)).send(captor.capture());
        assertEquals("patient22@example.com", captor.getValue().getFrom());
        assertEquals("Add drug", captor.getValue().getSubject());
        assertEquals("Please add this drug", captor.getValue().getText());
    }

    @Test
    void sendPrescriptionConfirmationEmail_shouldSendMimeMessage() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        User user = user("patient23@example.com");

        emailService.sendPrescriptionConfirmationEmail(user, "Doctor", "prescription-001");

        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));
    }

    private User user(String email) {
        return User.builder()
                .username(email.substring(0, email.indexOf('@')))
                .password("password")
                .firstName("Patient")
                .lastName("Test")
                .email(email)
                .avatarImg("")
                .role(Role.USER)
                .verifyEmail(true)
                .build();
    }
}
