package com.ryo.identity.service.impl;

import com.ryo.identity.constant.TypeOfRequest;
import com.ryo.identity.dto.request.CreateSuggestionRequest;
import com.ryo.identity.entity.Request;
import com.ryo.identity.entity.User;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.exception.ErrorCode;
import com.ryo.identity.repository.RequestRepository;
import com.ryo.identity.repository.UserRepository;
import com.ryo.identity.service.IRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements IRequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Override
    public Request createRequest(CreateSuggestionRequest request) {
        // Lấy user hiện tại
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );

        Request newRequest = Request.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .typeOfRequest(request.getTypeOfRequest())
                .proceed(false)
                .user(user)
                .build();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(user.getEmail());
        message.setTo("admin@gmail.com");
        message.setSubject(request.getTitle());
        message.setText(request.getContent());
        mailSender.send(message);

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
