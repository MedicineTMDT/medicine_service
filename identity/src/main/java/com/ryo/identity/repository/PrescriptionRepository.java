package com.ryo.identity.repository;

import com.ryo.identity.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PrescriptionRepository extends JpaRepository<Prescription,String> {
    // search by name
    Page<Prescription> findByUserIdAndNameContainingIgnoreCase(Integer userId, String name, Pageable pageable);

    // filter by date range
    Page<Prescription> findByUserIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            Integer userId,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );
}
