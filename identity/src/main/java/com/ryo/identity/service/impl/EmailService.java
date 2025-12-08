package com.ryo.identity.service.impl;

import com.ryo.identity.entity.User;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailAddress;

    @Async
    public void sendPasswordChangeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailAddress);
            message.setTo(user.getEmail());
            message.setSubject("Mật khẩu của bạn đã được thay đổi");
            message.setText(
                    "Xin chào " + user.getFirstName() + ",\n\n" +
                            "Mật khẩu tài khoản của bạn vừa được thay đổi thành công.\n" +
                            "Nếu bạn không thực hiện hành động này, vui lòng liên hệ ngay với bộ phận hỗ trợ.\n\n" +
                            "Trân trọng,\n" +
                            "Đội ngũ hỗ trợ"
            );

            mailSender.send(message);
            log.info("Email sent successfully to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }

    @Async
    public void sendOtp(User user, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailAddress);
            message.setTo(user.getEmail());
            message.setSubject("Mã xác thực OTP");
            message.setText("Mã OTP của bạn là: " + otp + "\nMã có hiệu lực trong 5 phút.");

            mailSender.send(message);
            log.info("Email sent successfully to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }

    @Async
    public void getRequest(User user, String title, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(user.getEmail());
            message.setTo("admin@gmail.com");
            message.setSubject(title);
            message.setText(content);
            mailSender.send(message);
            log.info("Email sent successfully to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }
}
