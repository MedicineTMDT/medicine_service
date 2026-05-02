package com.ryo.medicine.service;

import com.ryo.medicine.constant.TypeOfRequest;
import com.ryo.medicine.dto.request.CreateSuggestionRequest;
import com.ryo.medicine.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRequestService {
    Request createRequest(CreateSuggestionRequest request);
    Page<Request> getAllRequest(Pageable pageable);
    Page<Request> getAllRequestByTypeOfReques(Pageable pageable, TypeOfRequest typeOfRequest);
    Page<Request> getAllRequestByUserId(Pageable pageable, String userId);


}
