package com.ryo.request.repository;

import com.ryo.request.entity.Intake;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntakeRepository extends JpaRepository<Intake, String> {
}
