package com.ryo.medicine.service.impl;

import com.ryo.medicine.constant.TypeOfRequest;
import com.ryo.medicine.dto.request.CreateSuggestionRequest;
import com.ryo.medicine.dto.response.NotificationResponse;
import com.ryo.medicine.entity.Request;
import com.ryo.medicine.entity.User;
import com.ryo.medicine.exception.AppException;
import com.ryo.medicine.exception.ErrorCode;
import com.ryo.medicine.repository.RequestRepository;
import com.ryo.medicine.repository.UserRepository;
import com.ryo.medicine.service.IRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements IRequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final EmailService emailService;

    private void sendNotificationToAdmin(String userEmail) {
        NotificationResponse noti = NotificationResponse.builder()
                .message("New request created by: " + userEmail)
                .time(LocalDateTime.now())
                .build();

        // Tất cả admin đang subscribe /topic/admin sẽ nhận được
        simpMessagingTemplate.convertAndSend("/topic/admin", noti);
    }

    @Override
    public Request createRequest(CreateSuggestionRequest request) {
        // Lấy user hiện tại
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );

        Request newRequest = Request.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .typeOfRequest(request.getTypeOfRequest())
                .proceed(false)
                .user(user)
                .build();
        emailService.getRequest(user,request.getTitle(), request.getContent());


        sendNotificationToAdmin(user.getEmail());

        return requestRepository.save(newRequest);
    }

    @Override
    public Page<Request> getAllRequest(Pageable pageable) {
        return requestRepository.findAll(pageable);
    }

    @Override
    public Page<Request> getAllRequestByTypeOfReques(Pageable pageable, TypeOfRequest typeOfRequest) {
        return requestRepository.findByTypeOfRequest(typeOfRequest, pageable);
    }

    @Override
    public Page<Request> getAllRequestByUserId(Pageable pageable, String userId) {
        return requestRepository.findByUserId(userId, pageable);
    }
}
