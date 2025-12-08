package com.ryo.identity.controller;

import com.ryo.identity.dto.request.CreatePrescriptionRequest;
import com.ryo.identity.dto.response.PrescriptionInfo;
import com.ryo.identity.entity.Prescription;
import com.ryo.identity.service.IPrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1//prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final IPrescriptionService prescriptionService;

    // -----------------------------
    // CREATE PRESCRIPTION (MED only)
    // -----------------------------
    @PostMapping
    public Prescription createPrescription(
            @RequestBody CreatePrescriptionRequest request,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        return prescriptionService.createPrescription(request, userId);
    }

    // -----------------------------
    // COPY PRESCRIPTION
    // -----------------------------
    @PostMapping("/{id}/copy")
    public Prescription copyPrescription(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        return prescriptionService.copyPrescription(id, userId);
    }

    // -----------------------------
    // SEARCH BY NAME
    // -----------------------------
    @GetMapping("/search/name")
    public Page<Prescription> searchByName(
            @RequestParam Integer userId,
            @RequestParam String name,
            Pageable pageable
    ) {
        return prescriptionService.searchByName(userId, name, pageable);
    }

    // -----------------------------
    // SEARCH BY DATE RANGE
    // -----------------------------
    @GetMapping("/search/date")
    public Page<Prescription> searchByDate(
            @RequestParam Integer userId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            Pageable pageable
    ) {
        return prescriptionService.searchByDate(userId, start, end, pageable);
    }

    // -----------------------------
    // REVIEW DRUG INTERACTION
    // -----------------------------
    @PostMapping("/review")
    public PrescriptionInfo getPrescriptionReview(
            @RequestBody List<Integer> listDrugIds
    ) {
        return prescriptionService.getPrescriptionReview(listDrugIds);
    }
}
