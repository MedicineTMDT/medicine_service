package com.ryo.identity.repository;

import com.ryo.identity.entity.Intake;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntakeRepository extends JpaRepository<Intake, String> {
}
