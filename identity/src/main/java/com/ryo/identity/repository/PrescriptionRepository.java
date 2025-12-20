package com.ryo.identity.repository;

import com.ryo.identity.entity.Prescription;
import com.ryo.identity.projection.PrescriptionProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
public interface PrescriptionRepository extends JpaRepository<Prescription,String> {
    // search by name
    Page<PrescriptionProjection> findByUser_IdAndNameContainingIgnoreCaseAndActivateTrue(String userId, String name, Pageable pageable);

    // filter by date range
    Page<PrescriptionProjection> findByUser_IdAndStartDateGreaterThanEqualAndEndDateLessThanEqualAndActivateTrue(
            String userId,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    Optional<Prescription> findByUser_IdAndIdAndActivateTrue(String userId, String id);
    Optional<Prescription> findByUser_IdAndId(String userId, String id);
    Optional<Prescription> findByPatient_IdAndId(String patientId, String id);
    Prescription findByPatient_IdAndOrgPrescriptionIdAndActivateTrue(String patientId, String orgPresId);
}
