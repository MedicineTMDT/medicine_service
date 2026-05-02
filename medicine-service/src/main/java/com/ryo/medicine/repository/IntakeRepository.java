package com.ryo.medicine.repository;

import com.ryo.medicine.entity.Intake;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntakeRepository extends JpaRepository<Intake, String> {
}
