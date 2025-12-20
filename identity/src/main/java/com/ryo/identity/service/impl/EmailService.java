package com.ryo.identity.service.impl;

import com.ryo.identity.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailAddress;

    @Value("${frontend.url}")
    private String fronend_url;

    @Value("${frontend.accept_prescription_endpoint}")
    private String accept_prescription_endpoint;

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
            message.setTo(emailAddress);
            message.setSubject(title);
            message.setText(content);
            mailSender.send(message);
            log.info("Email sent successfully to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }

    @Async
    public void sendPrescriptionConfirmationEmail(
            User patient,
            String doctorName,
            String prescriptionId
    ) {
        try {
            String confirmUrl = fronend_url + accept_prescription_endpoint + prescriptionId;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(emailAddress);
            helper.setTo(patient.getEmail());
            helper.setSubject("Xác nhận đơn thuốc từ bác sĩ " + doctorName);

            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                            "<p>Xin chào <b>" + patient.getFirstName() + "</b>,</p>" +

                            "<p>Bác sĩ <b>" + doctorName + "</b> vừa kê một đơn thuốc cho bạn.</p>" +

                            "<p>Vui lòng xác nhận đơn thuốc bằng cách nhấn vào nút bên dưới:</p>" +

                            "<div style='margin: 30px 0; text-align: center;'>" +
                            "  <a href='" + confirmUrl + "' " +
                            "     style='background-color: #28a745;" +
                            "            color: white;" +
                            "            padding: 14px 28px;" +
                            "            text-decoration: none;" +
                            "            border-radius: 6px;" +
                            "            font-size: 16px;" +
                            "            display: inline-block;'>" +
                            "     ✔ XÁC NHẬN ĐƠN THUỐC" +
                            "  </a>" +
                            "</div>" +

                            "<p>Nếu bạn không thực hiện hành động nào, đơn thuốc sẽ không được kích hoạt.</p>" +

                            "<p>Trân trọng,<br/>" +
                            "<b>Hệ thống quản lý thuốc</b></p>" +
                            "</div>";

            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(mimeMessage);
            log.info("Prescription confirmation email sent to {}", patient.getEmail());

        } catch (Exception e) {
            log.error("Failed to send prescription confirmation email", e);
        }
    }


}
