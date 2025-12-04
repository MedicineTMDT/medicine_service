package com.ryo.identity.controller;

import com.ryo.identity.dto.request.*;
import com.ryo.identity.dto.response.ApiResponse;
import com.ryo.identity.dto.response.AuthenticationResponse;
import com.ryo.identity.dto.response.IntrospectResponse;
import com.ryo.identity.dto.response.UserResponse;
import com.ryo.identity.service.impl.AuthenticationServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {

    AuthenticationServiceImpl authenticationService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ApiResponse.<UserResponse>builder()
                .result(authenticationService.createUser(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(
            @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(
                        authenticationService.authenticate(request)
                )
                .build();
    }

    @PostMapping("/verify-email")
    public ApiResponse<?> verifyEmail(
            @RequestParam String email,
            @RequestParam String token
    ) {
        authenticationService.verifyEmailAddres(email, token);
        return ApiResponse.builder()
                .message("Your Email has been verified")
                .build();
    }

    @PostMapping("/verify-forgot-password")
    public ApiResponse<AuthenticationResponse> verifyForgotPasswordToken(
            @RequestParam String email,
            @RequestParam String token
    ) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.verifyForgotPasswordToken(token, email))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(
            @Valid @RequestBody LogoutRequest request
    ) throws Exception {
        authenticationService.logout(request);
        return ApiResponse.builder()
                .message("Logout successfully")
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(
            @Valid @RequestBody IntrospectRequest request
    ) throws Exception {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }
}
