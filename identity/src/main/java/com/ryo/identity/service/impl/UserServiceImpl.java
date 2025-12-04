package com.ryo.identity.service.impl;

import com.ryo.identity.dto.request.EditUserRequest;
import com.ryo.identity.dto.response.UserResponse;
import com.ryo.identity.entity.User;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.exception.ErrorCode;
import com.ryo.identity.mapper.UserMapper;
import com.ryo.identity.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserServiceImpl {

    JavaMailSender mailSender;
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${spring.mail.username}")
    protected String emailAddress;

    @PreAuthorize("request.username == authentication.name")
    public UserResponse editUserInfo(@Valid @RequestBody EditUserRequest request){

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check username unique
        if (request.getUsername() != null && !request.getUsername().isBlank() &&
                userRepository.existsByUsernameAndIdNot(request.getUsername(), user.getId())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // Update fields
        userMapper.editUserRequest(user, request);
        userRepository.save(user);

        return userMapper.user2UserResponse(user);
    }

    @PreAuthorize("request.username == authentication.name")
    public void changeUserPassword(String newPassword){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user  = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

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
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.user2UserResponse(user);
    }

    public void forgotPassword(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailAddress);
        message.setTo(email);
        message.setSubject("Mã xác thực OTP");
        message.setText("Mã OTP của bạn là: " + otp + "\nMã có hiệu lực trong 5 phút.");

        mailSender.send(message);

        user.setForgotPasswordToken(otp);
        userRepository.save(user);
    }
}
