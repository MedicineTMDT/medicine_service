package com.ryo.identity.controller;

import com.ryo.identity.dto.request.EditUserRequest;
import com.ryo.identity.dto.response.UserResponse;
import com.ryo.identity.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserServiceImpl userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return ResponseEntity.ok("OTP đã được gửi vào email.");
    }

    @PutMapping("/edit")
    public ResponseEntity<UserResponse> editUserInfo(
            @Valid @RequestBody EditUserRequest request
    ) {
        return ResponseEntity.ok(userService.editUserInfo(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam String newPassword
    ) {
        userService.changeUserPassword(newPassword);
        return ResponseEntity.ok("Đổi mật khẩu thành công.");
    }

}
