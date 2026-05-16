package com.ryo.identity.service;

import com.ryo.identity.constant.Role;
import com.ryo.identity.constant.TypeOfRequest;
import com.ryo.identity.dto.request.CreateSuggestionRequest;
import com.ryo.identity.entity.Request;
import com.ryo.identity.entity.User;
import com.ryo.identity.repository.RequestRepository;
import com.ryo.identity.repository.UserRepository;
import com.ryo.identity.service.impl.RequestServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class RequestServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private RequestServiceImpl requestService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createRequest_shouldPersistRequestForAuthenticatedUser() {
        User user = saveUser("request01", "request01@example.com");
        authenticateAs(user.getId(), "USER");
        CreateSuggestionRequest request = new CreateSuggestionRequest();
        request.setTitle("Add medicine");
        request.setContent("Please add a new medicine");
        request.setTypeOfRequest(TypeOfRequest.ADD);

        Request result = requestService.createRequest(request);

        assertNotNull(result.getId());
        assertFalse(result.getProceed());
        assertEquals(user.getId(), result.getUser().getId());
    }

    @Test
    void getAllRequestByTypeOfReques_shouldFilterByType() {
        User user = saveUser("request02", "request02@example.com");
        requestRepository.save(Request.builder()
                .title("Add")
                .content("Add")
                .typeOfRequest(TypeOfRequest.ADD)
                .proceed(false)
                .user(user)
                .build());
        requestRepository.save(Request.builder()
                .title("Question")
                .content("Question")
                .typeOfRequest(TypeOfRequest.QUESTION)
                .proceed(false)
                .user(user)
                .build());

        Page<Request> result =
                requestService.getAllRequestByTypeOfReques(PageRequest.of(0, 10), TypeOfRequest.ADD);

        assertEquals(1, result.getTotalElements());
        assertEquals("Add", result.getContent().getFirst().getTitle());
    }

    @Test
    void updateRequest_shouldOnlyUpdateProceedStatus() {
        User user = saveUser("request03", "request03@example.com");
        Request saved = requestRepository.save(Request.builder()
                .title("Title")
                .content("Content")
                .typeOfRequest(TypeOfRequest.EDIT)
                .proceed(false)
                .user(user)
                .build());

        Request patch = Request.builder().proceed(true).build();
        Request result = requestService.updateRequest(saved.getId(), patch);

        assertTrue(result.getProceed());
        assertEquals("Title", result.getTitle());
    }

    @Test
    void deleteRequest_shouldRemoveRequest() {
        User user = saveUser("request04", "request04@example.com");
        Request saved = requestRepository.save(Request.builder()
                .title("Delete")
                .content("Delete")
                .typeOfRequest(TypeOfRequest.ADD)
                .proceed(false)
                .user(user)
                .build());

        requestService.deleteRequest(saved.getId());

        assertFalse(requestRepository.existsById(saved.getId()));
    }

    private User saveUser(String username, String email) {
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .firstName("First")
                .lastName("Last")
                .email(email)
                .avatarImg("")
                .role(Role.USER)
                .verifyEmail(true)
                .build());
    }
}
