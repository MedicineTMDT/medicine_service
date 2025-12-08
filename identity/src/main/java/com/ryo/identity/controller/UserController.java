package com.ryo.identity.controller;

import com.ryo.identity.dto.request.EditUserRequest;
import com.ryo.identity.dto.response.ApiResponse;
import com.ryo.identity.dto.response.UserResponse;
import com.ryo.identity.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserServiceImpl userService;

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable String id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(id))
                .build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<?> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return ApiResponse.builder()
                .message("OTP code has been sent to your email")
                .build();
    }

    @PutMapping("/edit")
    public ApiResponse<UserResponse> editUserInfo(
            @Valid @RequestBody EditUserRequest request
    ) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.editUserInfo(request))
                .build();
    }

    @PutMapping("/change-password")
    public ApiResponse<?> changePassword(
            @RequestParam String newPassword
    ) {
        userService.changeUserPassword(newPassword);
        return ApiResponse.builder()
                .message("Your password has been changed successfully")
                .build();
    }

    @PutMapping("/update-avatar-img")
    public ApiResponse<String> updateAvatarImg(@RequestParam("file") MultipartFile file) {
        String imageUrl = userService.updateAvatarImg(file);
        return ApiResponse.<String>builder()
                .result(imageUrl)
                .build();
    }
}
