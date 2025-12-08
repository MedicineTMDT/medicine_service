package com.ryo.identity.service.impl;

import com.ryo.identity.dto.request.CreatePrescriptionRequest;
import com.ryo.identity.dto.request.IntakeItemRequest;
import com.ryo.identity.dto.request.IntakeRequest;
import com.ryo.identity.dto.response.PrescriptionInfo;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements IPrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final DrugRepository drugRepository;
    private final DrugInteractionServiceImpl drugInteractionService;

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
                .info(original.getInfo())
                .drugInteractionResponseList(original.getDrugInteractionResponseList())
                .user(user)
                .build();

        List<Intake> newIntakes = new ArrayList<>();

        for (Intake intake : original.getIntakes()) {

            Intake newIntake = Intake.builder()
                    .time(intake.getTime())
                    .status("PENDING")
                    .prescription(copy)
                    .build();

            List<IntakeItem> newItems = new ArrayList<>();

            for (IntakeItem item : intake.getItems()) {
                IntakeItem newItem = IntakeItem.builder()
                        .intake(newIntake)
                        .drug(item.getDrug())
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

    @Override
    public PrescriptionInfo getPrescriptionReview(List<Integer> listDrug) {
        List<Map<String,Map<String, Object>>> info = new ArrayList<>();
        List<Map<String, String>> drugInteractionResponseList = new ArrayList<>();
        Set<String> list_ingredient = new HashSet<>();
        for (Integer drugId : listDrug) {
            Drug drug = drugRepository.findById(drugId)
                    .orElseThrow(() -> new AppException(ErrorCode.DRUG_NOT_EXIST));
            Map<String, Object> innerMap = drug.getInfo();
            Map<String, Map<String, Object>> outerMap = new HashMap<>();
            outerMap.put(drug.getName(), innerMap);
            info.add(outerMap);

            Set<String> ingredientIds = drug.getMergedIngredients().stream()
                    .map(MergedIngredient::getName)
                    .collect(Collectors.toSet());
            List<DrugInteraction> interactionList =
                    drugInteractionService.getByListIngredientName(ingredientIds.stream().toList());

            for(DrugInteraction interaction: interactionList){
                Map<String, String> drugInteraction = new HashMap<>();
                drugInteraction.put("mucDoNghiemTrong", interaction.getMucDoNghiemTrong());
                drugInteraction.put("hauQuaCuaTuongTac", interaction.getHauQuaCuaTuongTac());
                drugInteraction.put("coCheTuongTac", interaction.getCoCheTuongTac());
                drugInteraction.put("xuTriTuongTac", interaction.getXuTriTuongTac());
                drugInteractionResponseList.add(drugInteraction);
            }

        }
        return PrescriptionInfo.builder()
                .info(info)
                .drugInteractionResponseList(drugInteractionResponseList)
                .build();

    }


}
