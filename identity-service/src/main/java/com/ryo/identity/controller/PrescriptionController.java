package com.ryo.identity.controller;

import com.ryo.identity.dto.request.CreatePrescriptionRequest;
import com.ryo.identity.dto.response.PrescriptionInfo;
import com.ryo.identity.entity.Intake;
import com.ryo.identity.entity.Prescription;
import com.ryo.identity.projection.PrescriptionProjection;
import com.ryo.identity.service.IPrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
@Slf4j
public class PrescriptionController {

    private final IPrescriptionService prescriptionService;

    @PostMapping("/scan")
    public CreatePrescriptionRequest scanPrescriptionImage(
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = image.getContentType();
        return prescriptionService.extractPrescriptionFromImage(base64Image, mimeType);
    }
    // -----------------------------
    // CREATE PRESCRIPTION (MED only)
    // -----------------------------
    @PostMapping
    public Prescription createPrescription(
            @RequestBody CreatePrescriptionRequest request
    ) {
        return prescriptionService.createPrescription(request);
    }

    // -----------------------------
    // COPY PRESCRIPTION
    // -----------------------------
    @PostMapping("/{id}/copy")
    public Prescription copyPrescription(
            @PathVariable String id
    ) {
        return prescriptionService.copyPrescription(id);
    }

    // -----------------------------
    // SEARCH BY NAME
    // -----------------------------
    @GetMapping("/search/name")
    public Page<PrescriptionProjection> searchByName(
            @RequestParam String name,
            Pageable pageable
    ) {
        return prescriptionService.searchByName(name, pageable);
    }

    // -----------------------------
    // SEARCH BY DATE RANGE
    // -----------------------------
    @GetMapping("/search/date")
    public Page<PrescriptionProjection> searchByDate(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            Pageable pageable
    ) {
        return prescriptionService.searchByDate(start, end, pageable);
    }

    // -----------------------------
    // REVIEW DRUG INTERACTION
    // -----------------------------
    @GetMapping("/review")
    public PrescriptionInfo getPrescriptionReview(
            @RequestParam List<Integer> listDrugIds
    ) {
        return prescriptionService.getPrescriptionReview(listDrugIds);
    }

    @PutMapping("/edit/{id}")
    public Intake editIntakeStatus(
        @PathVariable String id
    ){
        return prescriptionService.updateIntakeById(id);
    }

    @GetMapping("/{id}")
    public Prescription getPrescriptionById(
            @PathVariable String id
    ){
        return prescriptionService.getById(id);
    }

    // -----------------------------
    // ACCEPT PRESCRIPTION (PATIENT)
    // -----------------------------
    @PutMapping("/{id}/accept")
    public void acceptPrescription(
            @PathVariable String id
    ) {
        prescriptionService.accept_prescription(id);
    }


    // -----------------------------
    // DOCTOR DELETE PRESCRIPTION
    // -----------------------------
    @DeleteMapping("/{id}/doctor")
    public void doctorDeletePrescription(
            @PathVariable String id
    ) {
        prescriptionService.doctor_delete_prescription(id);
    }

    // -----------------------------
    // PATIENT DELETE PRESCRIPTION
    // -----------------------------
    @DeleteMapping("/{id}/patient")
    public void patientDeletePrescription(
            @PathVariable String id
    ) {
        prescriptionService.user_delete_prescription(id);
    }

}
