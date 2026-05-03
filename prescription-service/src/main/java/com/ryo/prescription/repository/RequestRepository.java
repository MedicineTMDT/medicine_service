package com.ryo.prescription.repository;

import com.ryo.prescription.constant.TypeOfRequest;
import com.ryo.prescription.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, String> {
    Page<Request> findByTypeOfRequest(TypeOfRequest typeOfRequest, Pageable pageable);
    Page<Request> findByUserId(String userId, Pageable pageable);
}
