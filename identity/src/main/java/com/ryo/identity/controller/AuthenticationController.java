package com.ryo.identity.controller;

import com.ryo.identity.dto.request.*;
import com.ryo.identity.dto.response.APIResponse;
import com.ryo.identity.dto.response.AuthenticationResponse;
import com.ryo.identity.dto.response.IntrospectResponse;
import com.ryo.identity.dto.response.UserResponse;
import com.ryo.identity.service.impl.AuthenticationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j @Validated
@Tag(
        name = "Authentication API",
        description = "API quản lý đăng ký, đăng nhập, xác thực token, xác minh email, quên mật khẩu."
)
public class AuthenticationController {

    AuthenticationServiceImpl authenticationService;

    // -----------------------------------------------------------
    @PostMapping("/register")
    public APIResponse<UserResponse> register(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return APIResponse.<UserResponse>builder()
                .result(authenticationService.createUser(request))
                .build();
    }

    // -----------------------------------------------------------
    @PostMapping("/login")
    public APIResponse<AuthenticationResponse> authenticate(
            @Valid @RequestBody LoginRequest request
    ) {
        return APIResponse.<AuthenticationResponse>builder()
                .result(authenticationService.authenticate(request))
                .build();
    }

    // -----------------------------------------------------------
    @PostMapping("/verify-email")
    public APIResponse<?> verifyEmail(
            @Parameter(description = "Email người dùng cần xác minh", example = "user@gmail.com")
            @RequestParam String email,

            @Parameter(description = "Token gửi tới email", example = "ABCD1234")
            @RequestParam String token
    ) {
        authenticationService.verifyEmailAddres(email, token);
        return APIResponse.builder()
                .message("Your Email has been verified")
                .build();
    }

    // -----------------------------------------------------------
    @PostMapping("/verify-forgot-password")
    public APIResponse<AuthenticationResponse> verifyForgotPasswordToken(
            @Parameter(description = "Email đăng ký", example = "user@gmail.com")
            @RequestParam String email,

            @Parameter(description = "Token reset mật khẩu", example = "XYZ12345")
            @RequestParam String token
    ) {
        return APIResponse.<AuthenticationResponse>builder()
                .result(authenticationService.verifyForgotPasswordToken(token, email))
                .build();
    }

    // -----------------------------------------------------------
    @PostMapping("/logout")
    public APIResponse<?> logout(
            @Valid @RequestBody LogoutRequest request
    ) throws Exception {
        authenticationService.logout(request);
        return APIResponse.builder()
                .message("Logout successfully")
                .build();
    }

    // -----------------------------------------------------------
    @PostMapping("/introspect")
    public APIResponse<IntrospectResponse> introspect(
            @Valid @RequestBody IntrospectRequest request
    ) throws Exception {
        return APIResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }
}
