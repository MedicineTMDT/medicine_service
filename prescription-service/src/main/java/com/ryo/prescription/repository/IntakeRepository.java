package com.ryo.prescription.repository;

import com.ryo.prescription.entity.Intake;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntakeRepository extends JpaRepository<Intake, String> {
}
