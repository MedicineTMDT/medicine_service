package com.ryo.identity.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl {

    JavaMailSender mailSender;
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    Cloudinary cloudinary;
    EmailService emailService;

    @NonFinal
    @Value("${spring.mail.username}")
    protected String emailAddress;

    public UserResponse editUserInfo(EditUserRequest request){
        log.info("BEGIN_EDIT_USER_INFO");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Current Username: " + userId);
        User user = userRepository.findById(userId)
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

    public void changeUserPassword(String newPassword){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("username: " + username);
        User user  = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendPasswordChangeEmail(user);
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.user2UserResponse(user);
    }

    public void forgotPassword(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
        emailService.sendOtp(user,otp);

        user.setForgotPasswordToken(otp);
        userRepository.save(user);
    }

    public String updateAvatarImg(MultipartFile file) {
        try {
            // Lấy email từ SecurityContext
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            // Upload ảnh lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto")
            );
            // Lấy URL ảnh
            String url = uploadResult.get("secure_url").toString();
            user.setAvatarImg(url);

            userRepository.save(user);
            return url;

        } catch (IOException e) {
            // Lỗi đọc file hoặc upload ảnh
            log.error("Upload avatar failed (IO Exception): {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);

        } catch (AppException e) {
            // Các lỗi đã được định nghĩa trong hệ thống
            log.error("AppException: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            // Lỗi không mong muốn (Cloudinary, NullPointer, ...)
            log.error("Unexpected error while updating avatar: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public UserResponse getUserByUserName(String userName){
        User user = userRepository.findByUsername(userName).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        return userMapper.user2UserResponse(user);
    }

}
