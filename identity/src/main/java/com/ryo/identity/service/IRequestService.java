package com.ryo.identity.service;

import com.ryo.identity.constant.TypeOfRequest;
import com.ryo.identity.dto.request.CreateSuggestionRequest;
import com.ryo.identity.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRequestService {
    Request createRequest(CreateSuggestionRequest request);
    Page<Request> getAllRequest(Pageable pageable);
    Page<Request> getAllRequestByTypeOfReques(Pageable pageable, TypeOfRequest typeOfRequest);
    Page<Request> getAllRequestByUserId(Pageable pageable, String userId);
}
