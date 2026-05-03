package com.ryo.request.service;

import com.ryo.request.entity.Intake;
import com.ryo.request.entity.Prescription;
import com.ryo.request.projection.PrescriptionProjection;
import com.ryo.request.dto.request.CreatePrescriptionRequest;
import com.ryo.request.dto.response.PrescriptionInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IPrescriptionService {
    Prescription createPrescription(CreatePrescriptionRequest request);
    Prescription copyPrescription(String prescriptionId);
    Page<PrescriptionProjection> searchByName(String name, Pageable pageable);
    Page<PrescriptionProjection> searchByDate(LocalDate start, LocalDate end, Pageable pageable);
    PrescriptionInfo getPrescriptionReview(List<Integer> listDrug);
    Prescription getById(String prescriptionId);
    Intake updateIntakeById(String id);
    void accept_prescription(String prescriptionId);
    void doctor_delete_prescription(String prescriptionId);
    void user_delete_prescription(String prescriptionId);
    String ask(String prompt);
    String askWithSystem(String systemPrompt, String userMessage);
    CreatePrescriptionRequest extractPrescriptionFromImage(String base64Image, String mimeType);
}
