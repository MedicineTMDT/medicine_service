package com.ryo.prescription.service;

import com.ryo.prescription.constant.TypeOfRequest;
import com.ryo.prescription.dto.request.CreateSuggestionRequest;
import com.ryo.prescription.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRequestService {
    Request createRequest(CreateSuggestionRequest request);
    Page<Request> getAllRequest(Pageable pageable);
    Page<Request> getAllRequestByTypeOfReques(Pageable pageable, TypeOfRequest typeOfRequest);
    Page<Request> getAllRequestByUserId(Pageable pageable, String userId);


}
