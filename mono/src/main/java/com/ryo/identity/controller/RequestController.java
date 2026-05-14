package com.ryo.identity.controller;

import com.ryo.identity.constant.TypeOfRequest;
import com.ryo.identity.dto.request.CreateSuggestionRequest;
import com.ryo.identity.dto.response.APIResponse;
import com.ryo.identity.entity.Request;
import com.ryo.identity.service.IRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public APIResponse<Request> createRequest(
            @RequestBody CreateSuggestionRequest createRequest
    ) {
        return APIResponse.<Request>builder()
                .result(requestService.createRequest(createRequest))
                .build();
    }

    // ------------------------------------
    // GET ALL REQUEST (admin xem mọi yêu cầu)
    // ------------------------------------
    @GetMapping
    public APIResponse<Page<Request>> getAllRequest(Pageable pageable) {
        return APIResponse.<Page<Request>>builder()
                .result(requestService.getAllRequest(pageable))
                .build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteRequest(@PathVariable String id) {
        requestService.deleteRequest(id);
        return APIResponse.<Void>builder()
                .message("Request deleted successfully")
                .build();
    }

    @PutMapping("/{id}")
    public APIResponse<Request> updateRequest(
            @PathVariable String id,
            @RequestBody Request request
    ) {
        return APIResponse.<Request>builder()
                .result(requestService.updateRequest(id, request))
                .build();
    }

    @GetMapping("/{id}")
    public APIResponse<Request> getRequestById(@PathVariable String id) {
        return APIResponse.<Request>builder()
                .result(requestService.getRequestById(id))
                .build();
    }

    // ------------------------------------
    // GET REQUEST BY TYPE (BUG, UI, FEATURE, etc.)
    // ------------------------------------
    @GetMapping("/type")
    public APIResponse<Page<Request>> getByType(
            Pageable pageable,
            @RequestParam TypeOfRequest type
    ) {
        return APIResponse.<Page<Request>>builder()
                .result(requestService.getAllRequestByTypeOfReques(pageable, type))
                .build();
    }

    // ------------------------------------
    // GET REQUEST BY USER
    // ------------------------------------
    @GetMapping("/user/{userId}")
    public APIResponse<Page<Request>> getByUser(
            Pageable pageable,
            @PathVariable String userId
    ) {
        return APIResponse.<Page<Request>>builder()
                .result(requestService.getAllRequestByUserId(pageable, userId))
                .build();
    }
}
