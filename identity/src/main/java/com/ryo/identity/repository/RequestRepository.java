package com.ryo.identity.repository;

import com.ryo.identity.constant.TypeOfRequest;
import com.ryo.identity.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, String> {
    Page<Request> findByTypeOfRequest(TypeOfRequest typeOfRequest, Pageable pageable);
}
