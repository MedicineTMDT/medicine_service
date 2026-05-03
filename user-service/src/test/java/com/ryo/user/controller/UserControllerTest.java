package com.ryo.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryo.user.constant.Role;
import com.ryo.user.dto.request.ChangePasswordRequest;
import com.ryo.user.dto.request.EditUserRequest;
import com.ryo.user.dto.response.UserResponse;
import com.ryo.user.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Slf4j
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserServiceImpl userService;

    private ObjectMapper objectMapper;

    private UserResponse userResponse;
    private EditUserRequest editUserRequest;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userResponse = UserResponse.builder()
                .id("user-001")
                .email("thanhtung@gmail.com")
                .username("tung123")
                .firstName("Tung")
                .lastName("Bui")
                .avatarImg("https://cdn.example.com/avatar.jpg")
                .role(Role.USER)
                .build();

        editUserRequest = EditUserRequest.builder()
                .username("tung123")
                .firstName("Tung")
                .lastName("Bui")
                .avatarImg("https://cdn.example.com/avatar.jpg")
                .build();

        changePasswordRequest = ChangePasswordRequest.builder()
                .newPassword("newSecurePass123")
                .build();
    }

    // ════════════════════════════════════════════════════════════
    //  GET /users/{id}  — GET BY ID
    // ════════════════════════════════════════════════════════════

    @Test
    void getUserById_happyPath() throws Exception {
        // Given
        Mockito.when(userService.getUserById("user-001")).thenReturn(userResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/user-001"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value("user-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.email")
                        .value("thanhtung@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.username")
                        .value("tung123"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.role").value("USER"));
    }

    @Test
    void getUserById_notFound_returnsError() throws Exception {
        // Given
        Mockito.when(userService.getUserById("not-exist"))
                .thenThrow(new RuntimeException("User not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/not-exist"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void getUserById_adminRole() throws Exception {
        // Given — user là ADMIN
        UserResponse adminUser = UserResponse.builder()
                .id("user-admin")
                .username("adminuser")
                .role(Role.ADMIN)
                .build();
        Mockito.when(userService.getUserById("user-admin")).thenReturn(adminUser);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/user-admin"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.role").value("ADMIN"));
    }

    @Test
    void getUserById_medRole() throws Exception {
        // Given — user là bác sĩ MED
        UserResponse medUser = UserResponse.builder()
                .id("user-med")
                .username("bsnguyenvan")
                .role(Role.MED)
                .build();
        Mockito.when(userService.getUserById("user-med")).thenReturn(medUser);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/user-med"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.role").value("MED"));
    }

    // ════════════════════════════════════════════════════════════
    //  GET /users/username/{username}  — GET BY USERNAME
    // ════════════════════════════════════════════════════════════

    @Test
    void getUserByUsername_happyPath() throws Exception {
        // Given
        Mockito.when(userService.getUserByUserName("tung123")).thenReturn(userResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/username/tung123"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.username")
                        .value("tung123"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.firstName")
                        .value("Tung"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.lastName")
                        .value("Bui"));
    }

    @Test
    void getUserByUsername_notFound_returnsError() throws Exception {
        // Given
        Mockito.when(userService.getUserByUserName("unknown"))
                .thenThrow(new RuntimeException("User not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/username/unknown"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  POST /users/forgot-password  — FORGOT PASSWORD
    // ════════════════════════════════════════════════════════════

    @Test
    void forgotPassword_happyPath() throws Exception {
        // Given
        doNothing().when(userService).forgotPassword("thanhtung@gmail.com");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/forgot-password")
                        .with(csrf())
                        .param("email", "thanhtung@gmail.com"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("OTP code has been sent to your email"));
    }

    @Test
    void forgotPassword_emailNotRegistered_returnsError() throws Exception {
        // Given — email chưa đăng ký tài khoản
        doThrow(new RuntimeException("Email not found"))
                .when(userService).forgotPassword("notexist@gmail.com");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/forgot-password")
                        .with(csrf())
                        .param("email", "notexist@gmail.com"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void forgotPassword_missingEmailParam_returnsBadRequest() throws Exception {
        // Given — thiếu @RequestParam email
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/forgot-password")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  PUT /users/edit  — EDIT USER INFO
    // ════════════════════════════════════════════════════════════

    @Test
    void editUserInfo_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(editUserRequest);
        Mockito.when(userService.editUserInfo(ArgumentMatchers.any()))
                .thenReturn(userResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.username")
                        .value("tung123"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.firstName")
                        .value("Tung"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.lastName")
                        .value("Bui"));
    }

    @Test
    void editUserInfo_blankUsername_returnsBadRequest() throws Exception {
        // Given — @NotBlank trên username
        EditUserRequest badRequest = EditUserRequest.builder()
                .username("")
                .firstName("Tung")
                .lastName("Bui")
                .build();
        String content = objectMapper.writeValueAsString(badRequest);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void editUserInfo_usernameTooShort_returnsBadRequest() throws Exception {
        // Given — @Size(min=5) trên username
        EditUserRequest badRequest = EditUserRequest.builder()
                .username("ab")
                .firstName("Tung")
                .lastName("Bui")
                .build();
        String content = objectMapper.writeValueAsString(badRequest);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void editUserInfo_usernameTooLong_returnsBadRequest() throws Exception {
        // Given — @Size(max=20) trên username
        EditUserRequest badRequest = EditUserRequest.builder()
                .username("a".repeat(21))
                .firstName("Tung")
                .lastName("Bui")
                .build();
        String content = objectMapper.writeValueAsString(badRequest);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void editUserInfo_firstNameContainsNumber_returnsBadRequest() throws Exception {
        // Given — @Pattern(regexp = "^[a-zA-Z]+$") trên firstName
        EditUserRequest badRequest = EditUserRequest.builder()
                .username("tung123")
                .firstName("Tung1")
                .lastName("Bui")
                .build();
        String content = objectMapper.writeValueAsString(badRequest);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void editUserInfo_blankFirstName_returnsBadRequest() throws Exception {
        // Given — @NotBlank trên firstName
        EditUserRequest badRequest = EditUserRequest.builder()
                .username("tung123")
                .firstName("")
                .lastName("Bui")
                .build();
        String content = objectMapper.writeValueAsString(badRequest);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void editUserInfo_blankLastName_returnsBadRequest() throws Exception {
        // Given — @NotBlank trên lastName
        EditUserRequest badRequest = EditUserRequest.builder()
                .username("tung123")
                .firstName("Tung")
                .lastName("")
                .build();
        String content = objectMapper.writeValueAsString(badRequest);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void editUserInfo_nullAvatarImg_isAllowed() throws Exception {
        // Given — avatarImg không có @NotBlank nên null được chấp nhận
        EditUserRequest requestWithNullAvatar = EditUserRequest.builder()
                .username("tung123")
                .firstName("Tung")
                .lastName("Bui")
                .avatarImg(null)
                .build();
        String content = objectMapper.writeValueAsString(requestWithNullAvatar);
        Mockito.when(userService.editUserInfo(ArgumentMatchers.any()))
                .thenReturn(userResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void editUserInfo_serviceThrowsException_returnsError() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(editUserRequest);
        Mockito.when(userService.editUserInfo(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Username already taken"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  PUT /users/change-password  — CHANGE PASSWORD
    // ════════════════════════════════════════════════════════════

    @Test
    void changePassword_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(changePasswordRequest);
        doNothing().when(userService).changeUserPassword("newSecurePass123");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Your password has been changed successfully"));
    }

    @Test
    void changePassword_nullPassword_stillCallsService() throws Exception {
        // Given — ChangePasswordRequest không có @NotBlank
        ChangePasswordRequest nullPwdRequest = ChangePasswordRequest.builder()
                .newPassword(null)
                .build();
        String content = objectMapper.writeValueAsString(nullPwdRequest);
        doNothing().when(userService).changeUserPassword(null);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Your password has been changed successfully"));
    }

    @Test
    void changePassword_serviceThrowsException_returnsError() throws Exception {
        // Given — mật khẩu mới không đủ mạnh
        String content = objectMapper.writeValueAsString(changePasswordRequest);
        doThrow(new RuntimeException("Password too weak"))
                .when(userService).changeUserPassword(anyString());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  PUT /users/update-avatar-img  — UPDATE AVATAR
    // ════════════════════════════════════════════════════════════

    @Test
    void updateAvatar_happyPath() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-bytes".getBytes()
        );
        Mockito.when(userService.updateAvatarImg(ArgumentMatchers.any()))
                .thenReturn("https://cdn.example.com/new-avatar.jpg");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/users/update-avatar-img")
                        .file(file)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result")
                        .value("https://cdn.example.com/new-avatar.jpg"));
    }

    @Test
    void updateAvatar_pngFile_happyPath() throws Exception {
        // Given — ảnh PNG thay vì JPEG
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-png-bytes".getBytes()
        );
        Mockito.when(userService.updateAvatarImg(ArgumentMatchers.any()))
                .thenReturn("https://cdn.example.com/new-avatar.png");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/users/update-avatar-img")
                        .file(file)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result")
                        .value("https://cdn.example.com/new-avatar.png"));
    }

    @Test
    void updateAvatar_missingFile_returnsBadRequest() throws Exception {
        // Given — không đính kèm file
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/users/update-avatar-img")
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void updateAvatar_uploadFails_returnsError() throws Exception {
        // Given — upload lên cloud storage thất bại
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "bytes".getBytes()
        );
        Mockito.when(userService.updateAvatarImg(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Upload failed"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/users/update-avatar-img")
                        .file(file)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}