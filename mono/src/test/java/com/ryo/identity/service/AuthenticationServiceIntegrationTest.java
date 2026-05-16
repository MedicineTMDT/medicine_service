package com.ryo.identity.service;

import com.ryo.identity.constant.Role;
import com.ryo.identity.dto.request.CreateUserRequest;
import com.ryo.identity.dto.request.IntrospectRequest;
import com.ryo.identity.dto.request.LoginRequest;
import com.ryo.identity.dto.request.LogoutRequest;
import com.ryo.identity.dto.response.AuthenticationResponse;
import com.ryo.identity.dto.response.IntrospectResponse;
import com.ryo.identity.entity.User;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.repository.InvalidatedTokenRepository;
import com.ryo.identity.repository.UserRepository;
import com.ryo.identity.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private AuthenticationServiceImpl authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createUser_shouldPersistUnverifiedUserAndOtpToken() {
        authenticationService.createUser(CreateUserRequest.builder()
                .username("patient01")
                .password("password123")
                .firstName("Patient")
                .lastName("One")
                .email("patient01@example.com")
                .role(Role.USER)
                .build());

        User saved = userRepository.findByEmail("patient01@example.com").orElseThrow();
        assertFalse(saved.getVerifyEmail());
        assertNotNull(saved.getVerifyEmailToken());
        assertFalse(saved.getVerifyEmailToken().isBlank());
    }

    @Test
    void authenticate_shouldReturnTokenForVerifiedUser() {
        User user = userRepository.save(User.builder()
                .username("verified01")
                .password(passwordEncoder.encode("password123"))
                .firstName("Verified")
                .lastName("User")
                .email("verified01@example.com")
                .avatarImg("")
                .role(Role.USER)
                .verifyEmail(true)
                .build());

        AuthenticationResponse result = authenticationService.authenticate(LoginRequest.builder()
                .email("verified01@example.com")
                .password("password123")
                .build());

        assertTrue(result.isAuthenticated());
        assertEquals(user.getId(), result.getId());
        assertNotNull(result.getToken());
    }

    @Test
    void authenticate_whenEmailNotVerified_shouldThrowAppException() {
        userRepository.save(User.builder()
                .username("unverified01")
                .password(passwordEncoder.encode("password123"))
                .firstName("Unverified")
                .lastName("User")
                .email("unverified01@example.com")
                .avatarImg("")
                .role(Role.USER)
                .verifyEmail(false)
                .build());

        assertThrows(AppException.class, () -> authenticationService.authenticate(LoginRequest.builder()
                .email("unverified01@example.com")
                .password("password123")
                .build()));
    }

    @Test
    void verifyEmailAddres_shouldMarkEmailVerifiedWhenTokenMatches() {
        userRepository.save(User.builder()
                .username("verifyme01")
                .password(passwordEncoder.encode("password123"))
                .firstName("Verify")
                .lastName("Me")
                .email("verifyme01@example.com")
                .avatarImg("")
                .role(Role.USER)
                .verifyEmail(false)
                .verifyEmailToken("123456")
                .build());

        authenticationService.verifyEmailAddres("verifyme01@example.com", "123456");

        assertTrue(userRepository.findByEmail("verifyme01@example.com").orElseThrow().getVerifyEmail());
    }

    @Test
    void logout_shouldInvalidateToken() throws Exception {
        userRepository.save(User.builder()
                .username("logout01")
                .password(passwordEncoder.encode("password123"))
                .firstName("Logout")
                .lastName("User")
                .email("logout01@example.com")
                .avatarImg("")
                .role(Role.USER)
                .verifyEmail(true)
                .build());
        String token = authenticationService.authenticate(LoginRequest.builder()
                .email("logout01@example.com")
                .password("password123")
                .build()).getToken();

        authenticationService.logout(LogoutRequest.builder().token(token).build());

        assertEquals(1, invalidatedTokenRepository.count());
    }

    @Test
    void introspect_whenTokenIsValid_shouldReturnValidTrue() throws Exception {
        userRepository.save(User.builder()
                .username("intro01")
                .password(passwordEncoder.encode("password123"))
                .firstName("Intro")
                .lastName("User")
                .email("intro01@example.com")
                .avatarImg("")
                .role(Role.USER)
                .verifyEmail(true)
                .build());
        String token = authenticationService.authenticate(LoginRequest.builder()
                .email("intro01@example.com")
                .password("password123")
                .build()).getToken();

        IntrospectResponse result = authenticationService.introspect(
                IntrospectRequest.builder().token(token).build());

        assertTrue(result.isValid());
    }
}
