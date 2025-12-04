package com.ryo.identity.repository;

import com.ryo.identity.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, String> {
}
