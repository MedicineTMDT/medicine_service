package com.ryo.identity.service;

import com.ryo.identity.constant.Role;
import com.ryo.identity.dto.request.CreateUserRequest;
import com.ryo.identity.dto.request.EditUserRequest;
import com.ryo.identity.dto.response.UserResponse;
import com.ryo.identity.entity.User;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.repository.UserRepository;
import com.ryo.identity.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createUser_shouldPersistVerifiedUser() {
        authenticateAs("admin", "ADMIN");

        UserResponse result = userService.createUser(CreateUserRequest.builder()
                .username("doctor01")
                .password("password123")
                .firstName("Doctor")
                .lastName("One")
                .email("doctor01@example.com")
                .role(Role.MED)
                .build());

        User saved = userRepository.findById(result.getId()).orElseThrow();
        assertTrue(saved.getVerifyEmail());
        assertNotEquals("password123", saved.getPassword());
        assertEquals(Role.MED, result.getRole());
    }

    @Test
    void editUserInfo_shouldUpdateAuthenticatedUser() {
        User user = saveUser("patient02", "patient02@example.com", Role.USER);
        authenticateAs(user.getId(), "USER");

        UserResponse result = userService.editUserInfo(EditUserRequest.builder()
                .username("patient02new")
                .firstName("New")
                .lastName("Name")
                .avatarImg("avatar.jpg")
                .build());

        assertEquals("patient02new", result.getUsername());
        assertEquals("avatar.jpg", userRepository.findById(user.getId()).orElseThrow().getAvatarImg());
    }

    @Test
    void editUserInfo_whenUsernameTaken_shouldThrowAppException() {
        User user = saveUser("patient03", "patient03@example.com", Role.USER);
        saveUser("taken03", "taken03@example.com", Role.USER);
        authenticateAs(user.getId(), "USER");

        assertThrows(AppException.class, () -> userService.editUserInfo(EditUserRequest.builder()
                .username("taken03")
                .firstName("Patient")
                .lastName("Three")
                .build()));
    }

    @Test
    void changeUserPassword_shouldPersistEncodedPassword() {
        User user = saveUser("patient04", "patient04@example.com", Role.USER);
        authenticateAs(user.getId(), "USER");

        userService.changeUserPassword("newPassword123");

        assertTrue(passwordEncoder.matches("newPassword123",
                userRepository.findById(user.getId()).orElseThrow().getPassword()));
    }

    @Test
    void forgotPassword_shouldSaveForgotPasswordToken() {
        saveUser("patient05", "patient05@example.com", Role.USER);

        userService.forgotPassword("patient05@example.com");

        String token = userRepository.findByEmail("patient05@example.com").orElseThrow()
                .getForgotPasswordToken();
        assertNotNull(token);
        assertEquals(6, token.length());
    }

    @Test
    void getAllUsers_shouldReturnUsersFromDatabase() {
        saveUser("patient06", "patient06@example.com", Role.USER);

        Page<UserResponse> result = userService.getAllUsers(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("patient06@example.com", result.getContent().getFirst().getEmail());
    }

    private User saveUser(String username, String email, Role role) {
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .firstName("First")
                .lastName("Last")
                .email(email)
                .avatarImg("")
                .role(role)
                .verifyEmail(true)
                .forgotPasswordToken("")
                .build());
    }
}
