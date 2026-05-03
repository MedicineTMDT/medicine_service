package com.ryo.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryo.request.dto.request.CreateUserRequest;
import com.ryo.request.dto.request.IntrospectRequest;
import com.ryo.request.dto.request.LoginRequest;
import com.ryo.request.dto.request.LogoutRequest;
import com.ryo.request.dto.response.AuthenticationResponse;
import com.ryo.request.dto.response.IntrospectResponse;
import com.ryo.request.dto.response.UserResponse;
import com.ryo.request.service.impl.AuthenticationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Slf4j
@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties")
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationServiceImpl authenticationService;

    private ObjectMapper objectMapper;

    // ── Request objects ──────────────────────────────────────────
    private CreateUserRequest createUserRequest;
    private LoginRequest loginRequest;
    private LogoutRequest logoutRequest;
    private IntrospectRequest introspectRequest;

    // ── Response objects ─────────────────────────────────────────
    private UserResponse userResponse;
    private AuthenticationResponse authenticationResponse;
    private IntrospectResponse introspectResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Requests
        createUserRequest = CreateUserRequest.builder()
                .username("tung123")
                .password("12314251")
                .firstName("Tung")
                .lastName("Bui")
                .email("thanhtunglk09123@gmail.com")
                .build();

        loginRequest = LoginRequest.builder()
                .email("thanhtunglk0911@gmail.com")
                .password("12345678")
                .build();

        logoutRequest = LogoutRequest.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid")
                .build();

        introspectRequest = IntrospectRequest.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid")
                .build();

        // Responses
        userResponse = UserResponse.builder()
                .id("cf0600f538b3")
                .build();

        authenticationResponse = AuthenticationResponse.builder()
                .email("thanhtunglk0911@gmail.com")
                .build();

        introspectResponse = IntrospectResponse.builder()
                .valid(true)
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid")
                .build();
    }

    // ════════════════════════════════════════════════════════════
    //  POST /auth/register
    // ════════════════════════════════════════════════════════════

    @Test
    void register_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(createUserRequest);
        Mockito.when(authenticationService.createUser(ArgumentMatchers.any()))
                .thenReturn(userResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value("cf0600f538b3"));
    }

    @Test
    void register_missingUsername_returnsBadRequest() throws Exception {
        // Given — username bị bỏ trống
        CreateUserRequest badRequest = CreateUserRequest.builder()
                .username("")
                .password("12314251")
                .firstName("Tung")
                .lastName("Bui")
                .email("thanhtunglk09123@gmail.com")
                .build();
        String content = objectMapper.writeValueAsString(badRequest);

        // When Then — Spring Validation chặn trước khi vào service
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void register_missingEmail_returnsBadRequest() throws Exception {
        // Given
        CreateUserRequest badRequest = CreateUserRequest.builder()
                .username("tung123")
                .password("12314251")
                .firstName("Tung")
                .lastName("Bui")
                .email("")
                .build();
        String content = objectMapper.writeValueAsString(badRequest);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  POST /auth/login
    // ════════════════════════════════════════════════════════════

    @Test
    void authenticate_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(loginRequest);
        Mockito.when(authenticationService.authenticate(ArgumentMatchers.any()))
                .thenReturn(authenticationResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.email")
                        .value("thanhtunglk0911@gmail.com"));
    }

    @Test
    void authenticate_emptyBody_returnsBadRequest() throws Exception {
        // Given — body rỗng
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void authenticate_serviceThrowsException_returnsError() throws Exception {
        // Given — service ném exception (sai mật khẩu, tài khoản bị khóa...)
        String content = objectMapper.writeValueAsString(loginRequest);
        Mockito.when(authenticationService.authenticate(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  POST /auth/verify-email
    // ════════════════════════════════════════════════════════════

    @Test
    void verifyEmail_happyPath() throws Exception {
        // Given
        doNothing().when(authenticationService)
                .verifyEmailAddres(anyString(), anyString());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-email")
                        .with(csrf())
                        .param("email", "thanhtunglk09123@gmail.com")
                        .param("token", "ABCD1234"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Your Email has been verified"));
    }

    @Test
    void verifyEmail_missingToken_returnsBadRequest() throws Exception {
        // Given — thiếu param token
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-email")
                        .with(csrf())
                        .param("email", "thanhtunglk09123@gmail.com"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void verifyEmail_missingEmail_returnsBadRequest() throws Exception {
        // Given — thiếu param email
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-email")
                        .with(csrf())
                        .param("token", "ABCD1234"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void verifyEmail_invalidToken_returnsError() throws Exception {
        // Given — token sai / hết hạn
        doThrow(new RuntimeException("Invalid or expired token"))
                .when(authenticationService)
                .verifyEmailAddres(anyString(), anyString());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-email")
                        .with(csrf())
                        .param("email", "thanhtunglk09123@gmail.com")
                        .param("token", "WRONG_TOKEN"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  POST /auth/verify-forgot-password
    // ════════════════════════════════════════════════════════════

    @Test
    void verifyForgotPassword_happyPath() throws Exception {
        // Given
        Mockito.when(authenticationService.verifyForgotPasswordToken(anyString(), anyString()))
                .thenReturn(authenticationResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-forgot-password")
                        .with(csrf())
                        .param("email", "thanhtunglk0911@gmail.com")
                        .param("token", "XYZ12345"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.email")
                        .value("thanhtunglk0911@gmail.com"));
    }

    @Test
    void verifyForgotPassword_missingParams_returnsBadRequest() throws Exception {
        // Given — thiếu cả 2 param
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-forgot-password")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void verifyForgotPassword_expiredToken_returnsError() throws Exception {
        // Given
        Mockito.when(authenticationService.verifyForgotPasswordToken(anyString(), anyString()))
                .thenThrow(new RuntimeException("Token expired"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-forgot-password")
                        .with(csrf())
                        .param("email", "thanhtunglk0911@gmail.com")
                        .param("token", "EXPIRED"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  POST /auth/logout
    // ════════════════════════════════════════════════════════════

    @Test
    void logout_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(logoutRequest);
        doNothing().when(authenticationService).logout(ArgumentMatchers.any());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Logout successfully"));
    }

    @Test
    void logout_blankToken_returnsBadRequest() throws Exception {
        // Given — @NotBlank trên LogoutRequest.token
        LogoutRequest badRequest = LogoutRequest.builder()
                .token("")
                .build();
        String content = objectMapper.writeValueAsString(badRequest);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void logout_invalidToken_returnsError() throws Exception {
        // Given — token đã bị blacklist hoặc hết hạn
        String content = objectMapper.writeValueAsString(logoutRequest);
        doThrow(new RuntimeException("Token invalid"))
                .when(authenticationService).logout(ArgumentMatchers.any());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  POST /auth/introspect
    // ════════════════════════════════════════════════════════════

    @Test
    void introspect_validToken() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(introspectRequest);
        Mockito.when(authenticationService.introspect(ArgumentMatchers.any()))
                .thenReturn(introspectResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/introspect")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.valid").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("result.token")
                        .value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid"));
    }

    @Test
    void introspect_invalidToken_returnsValidFalse() throws Exception {
        // Given — service trả về valid = false khi token hết hạn
        String content = objectMapper.writeValueAsString(introspectRequest);
        IntrospectResponse invalidResponse = IntrospectResponse.builder()
                .valid(false)
                .token(introspectRequest.getToken())
                .build();
        Mockito.when(authenticationService.introspect(ArgumentMatchers.any()))
                .thenReturn(invalidResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/introspect")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.valid").value(false));
    }

    @Test
    void introspect_nullToken_stillCallsService() throws Exception {
        // Given — IntrospectRequest không có @NotBlank nên null vẫn qua được
        IntrospectRequest nullTokenRequest = IntrospectRequest.builder()
                .token(null)
                .build();
        String content = objectMapper.writeValueAsString(nullTokenRequest);
        Mockito.when(authenticationService.introspect(ArgumentMatchers.any()))
                .thenReturn(IntrospectResponse.builder().valid(false).build());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/introspect")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.valid").value(false));
    }

    @Test
    void introspect_serviceThrowsException_returnsError() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(introspectRequest);
        Mockito.when(authenticationService.introspect(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("JWT parse error"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/introspect")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}