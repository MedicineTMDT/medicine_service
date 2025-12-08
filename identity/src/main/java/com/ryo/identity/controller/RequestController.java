package com.ryo.identity.controller;

import com.ryo.identity.constant.TypeOfRequest;
import com.ryo.identity.dto.request.CreateSuggestionRequest;
import com.ryo.identity.entity.Request;
import com.ryo.identity.service.IRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
@Tag(
        name = "Request API",
        description = "API lấy request theo id user, theo type của request, getAll , và tạo request."
)
public class RequestController {

    private final IRequestService requestService;

    // ------------------------------------
    // CREATE REQUEST (user gửi feedback)
    // ------------------------------------
    @PostMapping
    public Request createRequest(
            @RequestBody CreateSuggestionRequest createRequest
    ) {
        return requestService.createRequest(createRequest);
    }

    // ------------------------------------
    // GET ALL REQUEST (admin xem mọi yêu cầu)
    // ------------------------------------
    @GetMapping
    public Page<Request> getAllRequest(Pageable pageable) {
        return requestService.getAllRequest(pageable);
    }

    // ------------------------------------
    // GET REQUEST BY TYPE (BUG, UI, FEATURE, etc.)
    // ------------------------------------
    @GetMapping("/type")
    public Page<Request> getByType(
            Pageable pageable,
            @RequestParam TypeOfRequest type
    ) {
        return requestService.getAllRequestByTypeOfReques(pageable, type);
    }

    // ------------------------------------
    // GET REQUEST BY USER
    // ------------------------------------
    @GetMapping("/user/{userId}")
    public Page<Request> getByUser(
            Pageable pageable,
            @PathVariable String userId
    ) {
        return requestService.getAllRequestByUserId(pageable, userId);
    }
}
