package com.ryo.identity.service;

import com.ryo.identity.dto.request.CreatePrescriptionRequest;
import com.ryo.identity.dto.response.PrescriptionInfo;
import com.ryo.identity.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IPrescriptionService {
    Prescription createPrescription(CreatePrescriptionRequest request);
    Prescription createPrescription(Prescription prescription);
    Prescription copyPrescription(String prescriptionId);
    Page<Prescription> searchByName(Integer userId, String name, Pageable pageable);
    Page<Prescription> searchByDate(Integer userId, LocalDate start, LocalDate end, Pageable pageable);
    PrescriptionInfo getPrescriptionReview(List<Integer> listDrug);
}
