package com.ryo.identity.mapper;

import com.ryo.identity.constant.Role;
import com.ryo.identity.dto.request.CreateUserRequest;
import com.ryo.identity.dto.request.EditUserRequest;
import com.ryo.identity.dto.response.UserResponse;
import com.ryo.identity.entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserMapper {

    PasswordEncoder passwordEncoder;

    public User createUserRequest2User(CreateUserRequest reqest){
        return User.builder()
                .email(reqest.getEmail())
                .role(reqest.getRole())
                .username(reqest.getUsername())
                .lastName(reqest.getLastName())
                .firstName(reqest.getFirstName())
                .email(reqest.getEmail())
                .password(passwordEncoder.encode(reqest.getPassword()))
                .avatarImg("")
                .forgotPasswordToken("")
                .verifyEmail(false)
                .build();
    }

    public void editUserRequest(User user, EditUserRequest request) {

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getAvatarImg() != null) {
            user.setAvatarImg(request.getAvatarImg());
        }
    }


    public UserResponse user2UserResponse(User user){
        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .id(user.getId())
                .role(user.getRole())
                .build();
    }
}
