package com.ryo.request.service;

import com.ryo.request.constant.TypeOfRequest;
import com.ryo.request.dto.request.CreateSuggestionRequest;
import com.ryo.request.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRequestService {
    Request createRequest(CreateSuggestionRequest request);
    Page<Request> getAllRequest(Pageable pageable);
    Page<Request> getAllRequestByTypeOfReques(Pageable pageable, TypeOfRequest typeOfRequest);
    Page<Request> getAllRequestByUserId(Pageable pageable, String userId);


}
