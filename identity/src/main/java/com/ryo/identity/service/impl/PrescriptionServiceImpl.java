package com.ryo.identity.service.impl;

import com.ryo.identity.dto.request.CreatePrescriptionRequest;
import com.ryo.identity.dto.request.IntakeItemRequest;
import com.ryo.identity.dto.request.IntakeRequest;
import com.ryo.identity.entity.*;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.exception.ErrorCode;
import com.ryo.identity.repository.DrugRepository;
import com.ryo.identity.repository.PrescriptionRepository;
import com.ryo.identity.repository.UserRepository;
import com.ryo.identity.service.IPrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements IPrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final DrugRepository drugRepository;

    @Override
    @PreAuthorize("hasRole('MED')")
    public Prescription createPrescription(CreatePrescriptionRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Prescription prescription = Prescription.builder()
                .name(request.name())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .user(user)
                .build();

        List<Intake> intakeList = new ArrayList<>();

        for (IntakeRequest intakeReq : request.intakes()) {

            Intake intake = Intake.builder()
                    .time((LocalDateTime) intakeReq.time())
                    .status(intakeReq.status())
                    .prescription(prescription)
                    .build();

            List<IntakeItem> items = new ArrayList<>();

            for (IntakeItemRequest itemReq : intakeReq.items()) {

                Drug drug = drugRepository.findById(itemReq.drugId())
                        .orElseThrow(() -> new RuntimeException("Drug not found"));

                IntakeItem item = IntakeItem.builder()
                        .intake(intake)
                        .drug(drug)
                        .quantity(itemReq.quantity())
                        .build();

                items.add(item);
            }

            intake.setItems(items);
            intakeList.add(intake);
        }

        prescription.setIntakes(intakeList);

        return prescriptionRepository.save(prescription);
    }

    @Override
    public Prescription createPrescription(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    @Override
    public Prescription copyPrescription(String prescriptionId, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Prescription original = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        // Tạo bản clone của prescription
        Prescription copy = Prescription.builder()
                .name(original.getName() + " (Copy)")
                .description(original.getDescription())
                .startDate(LocalDate.from(LocalDateTime.now()))
                .endDate(original.getEndDate())
                .user(user)
                .build();

        List<Intake> newIntakes = new ArrayList<>();

        for (Intake intake : original.getIntakes()) {

            Intake newIntake = Intake.builder()
                    .time(intake.getTime())          // giữ nguyên hoặc tự reset tùy bạn
                    .status("PENDING")                // reset trạng thái
                    .prescription(copy)
                    .build();

            List<IntakeItem> newItems = new ArrayList<>();

            for (IntakeItem item : intake.getItems()) {
                IntakeItem newItem = IntakeItem.builder()
                        .intake(newIntake)
                        .drug(item.getDrug())         // giữ nguyên drug, không clone
                        .quantity(item.getQuantity())
                        .build();

                newItems.add(newItem);
            }

            newIntake.setItems(newItems);
            newIntakes.add(newIntake);
        }

        copy.setIntakes(newIntakes);

        return copy;
    }

    @Override
    public Page<Prescription> searchByName(Integer userId, String name, Pageable pageable) {
        return prescriptionRepository.findByUserIdAndNameContainingIgnoreCase(userId, name, pageable);
    }

    @Override
    public Page<Prescription> searchByDate(Integer userId, LocalDate start, LocalDate end, Pageable pageable) {
        return prescriptionRepository
                .findByUserIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(userId, start, end, pageable);
    }


}
