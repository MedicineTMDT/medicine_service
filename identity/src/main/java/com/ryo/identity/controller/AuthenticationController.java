package com.ryo.identity.controller;

import com.ryo.identity.dto.request.*;
import com.ryo.identity.dto.response.AuthenticationResponse;
import com.ryo.identity.dto.response.IntrospectResponse;
import com.ryo.identity.dto.response.UserResponse;
import com.ryo.identity.service.impl.AuthenticationServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationServiceImpl authenticationService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ResponseEntity.ok(authenticationService.createUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String email,
            @RequestParam String token
    ) {
        authenticationService.verifyEmailAddres(email, token);
        return ResponseEntity.ok("Email đã được xác thực!");
    }

    @PostMapping("/verify-forgot-password")
    public ResponseEntity<AuthenticationResponse> verifyForgotPasswordToken(
            @RequestParam String email,
            @RequestParam String token
    ) {
        return ResponseEntity.ok(authenticationService.verifyForgotPasswordToken(token, email));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid @RequestBody LogoutRequest request
    ) throws Exception {
        authenticationService.logout(request);
        return ResponseEntity.ok("Đăng xuất thành công!");
    }

    @PostMapping("/introspect")
    public ResponseEntity<IntrospectResponse> introspect(
            @Valid @RequestBody IntrospectRequest request
    ) throws Exception {
        return ResponseEntity.ok(authenticationService.introspect(request));
    }
}
