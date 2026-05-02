package com.ryo.medicine.service;

import com.ryo.medicine.dto.request.CreatePrescriptionRequest;
import com.ryo.medicine.dto.response.PrescriptionInfo;
import com.ryo.medicine.entity.Intake;
import com.ryo.medicine.entity.Prescription;
import com.ryo.medicine.projection.PrescriptionProjection;
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
