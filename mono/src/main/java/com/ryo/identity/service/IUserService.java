package com.ryo.identity.service;

import com.ryo.identity.dto.request.CreateUserRequest;
import com.ryo.identity.dto.request.EditUserRequest;
import com.ryo.identity.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUserByAdmin(String id, EditUserRequest request);
    void deleteUser(String id);
    UserResponse editUserInfo(EditUserRequest request);
    void changeUserPassword(String newPassword);
    UserResponse getUserById(String userId);
    void forgotPassword(String email);
    String updateAvatarImg(MultipartFile file);
    UserResponse getUserByUserName(String userName);
}
