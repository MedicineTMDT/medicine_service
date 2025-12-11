package com.ryo.identity.controller;

import com.ryo.identity.dto.request.ChangePasswordRequest;
import com.ryo.identity.dto.request.EditUserRequest;
import com.ryo.identity.dto.response.APIResponse;
import com.ryo.identity.dto.response.UserResponse;
import com.ryo.identity.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor @Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "User API",
        description = "API lấy thông tin user, quên mật khẩu, edit info user, đổi mật khẩu và thay avatar."
)
public class UserController {

    UserServiceImpl userService;

    @GetMapping("/{id}")
    public APIResponse<UserResponse> getUserById(@PathVariable String id) {
        return APIResponse.<UserResponse>builder()
                .result(userService.getUserById(id))
                .build();
    }

    @GetMapping("/{username}")
    public APIResponse<UserResponse> getUserByUserName(@PathVariable String username) {
        return APIResponse.<UserResponse>builder()
                .result(userService.getUserByUserName(username))
                .build();
    }

    @PostMapping("/forgot-password")
    public APIResponse<?> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return APIResponse.builder()
                .message("OTP code has been sent to your email")
                .build();
    }

    @PutMapping("/edit")
    public APIResponse<UserResponse> editUserInfo(
            @Valid @RequestBody EditUserRequest request
    ) {
        log.info("edit user endpoint");
        return APIResponse.<UserResponse>builder()
                .result(userService.editUserInfo(request))
                .build();
    }

    @PutMapping("/change-password")
    public APIResponse<?> changePassword(
            @RequestBody ChangePasswordRequest request
    ) {
        userService.changeUserPassword(request.getNewPassword());
        return APIResponse.builder()
                .message("Your password has been changed successfully")
                .build();
    }

    @PutMapping("/update-avatar-img")
    public APIResponse<String> updateAvatarImg(@RequestParam("file") MultipartFile file) {
        String imageUrl = userService.updateAvatarImg(file);
        return APIResponse.<String>builder()
                .result(imageUrl)
                .build();
    }
}
