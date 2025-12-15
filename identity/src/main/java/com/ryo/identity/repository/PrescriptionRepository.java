package com.ryo.identity.repository;

import com.ryo.identity.entity.Prescription;
import com.ryo.identity.projection.PrescriptionProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PrescriptionRepository extends JpaRepository<Prescription,String> {
    // search by name
    Page<PrescriptionProjection> findByUser_IdAndNameContainingIgnoreCase(Integer userId, String name, Pageable pageable);

    // filter by date range
    Page<PrescriptionProjection> findByUser_IdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            Integer userId,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    Prescription findByUser_IdAndId(String id);
}
