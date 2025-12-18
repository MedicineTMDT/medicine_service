package com.ryo.identity.service.impl;

import com.ryo.identity.dto.MedicationSchedule;
import com.ryo.identity.dto.request.CreatePrescriptionRequest;
import com.ryo.identity.dto.request.IntakeRequest;
import com.ryo.identity.dto.response.PrescriptionInfo;
import com.ryo.identity.entity.*;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.exception.ErrorCode;
import com.ryo.identity.projection.PrescriptionProjection;
import com.ryo.identity.repository.DrugRepository;
import com.ryo.identity.repository.IntakeRepository;
import com.ryo.identity.repository.PrescriptionRepository;
import com.ryo.identity.repository.UserRepository;
import com.ryo.identity.service.IPrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final IntakeRepository intakeRepository;

    @Override
    @PreAuthorize("hasAnyAuthority('MED', 'ADMIN')")
    public Prescription createPrescription(CreatePrescriptionRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // validate constraint
        for(IntakeRequest intakeRequest: request.intakes()){
            int sum = 0;
            for(MedicationSchedule item: intakeRequest.timingList()){
                sum+=item.getQuantity();
            }
            if(sum > intakeRequest.total()){
                throw new AppException(ErrorCode.INVALID_PRESCRIPTION);
            }
        }
        // build Intake here
        Map<LocalDateTime, List<Map<String, Object>>> seen = new HashMap<>();
        LocalDateTime max = LocalDateTime.MIN;
        LocalDate startDate = request.startDate();
        for(IntakeRequest intakeRequest: request.intakes()){
            int total = intakeRequest.total();
            while(total > 0){
                for(MedicationSchedule medicationSchedule: intakeRequest.timingList()){
                    if(total - medicationSchedule.getQuantity() >= 0){
                        // tạo khóa cho seen
                        LocalDateTime key = startDate.atTime(medicationSchedule.getTiming().getTime());
                        if(key.isAfter(max)){
                            max = key;
                        }
                        Map<String, Object> innerMap = new HashMap<>();
                        innerMap.put("drugName",intakeRequest.drugName());
                        innerMap.put("drugId",intakeRequest.drugId());
                        innerMap.put("usage",intakeRequest.usage());
                        innerMap.put("medicineForm",intakeRequest.medicineForm());
                        innerMap.put("noteList",intakeRequest.noteList());
                        if(seen.containsKey(key)){
                            seen.get(key).add(
                                    innerMap
                            );
                        }else{
                            seen.put(key,new ArrayList<>(List.of(innerMap)));
                        }
                    }
                    else{
                        break;
                    }
                    total -= medicationSchedule.getQuantity();
                    startDate = startDate.plusDays(1);
                }
            }
        }
        Prescription prescription = Prescription.builder()
                .name(request.name())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(max.toLocalDate())
                .message(request.message())
                .diagnosisNote(request.diagnosisNote())
                .info(request.info())
                .user(user)
                .build();
        List<Intake> intakeList = new ArrayList<>();
        for(Map.Entry<LocalDateTime,List<Map<String, Object>>> item: seen.entrySet()){
            Intake intake = Intake.builder()
                    .prescription(prescription)
                    .time(item.getKey())
                    .status(false)
                    .info(item.getValue())
                    .build();
            intakeList.add(intake);
        }
        prescription.setIntakes(intakeList);
        return prescriptionRepository.save(prescription);
    }

    @Override
    public Prescription copyPrescription(String prescriptionId) {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Prescription original = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        LocalDate newStartDate = LocalDate.now();

        long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(
                original.getStartDate(),
                newStartDate
        );

        Prescription copy = Prescription.builder()
                .name(original.getName())
                .description(original.getDescription())
                .startDate(newStartDate)
                .endDate(
                        original.getEndDate() != null
                                ? original.getEndDate().plusDays(dayOffset)
                                : null
                )
                .message(original.getMessage())
                .diagnosisNote(original.getDiagnosisNote())
                .info(original.getInfo())
                .user(user)
                .build();

        List<Intake> copiedIntakes = new ArrayList<>();

        for (Intake intake : original.getIntakes()) {

            Intake clonedIntake = Intake.builder()
                    .time(intake.getTime().plusDays(dayOffset)) // shift ngày
                    .status(false) // reset trạng thái
                    .info(intake.getInfo())
                    .prescription(copy)
                    .build();

            copiedIntakes.add(clonedIntake);
        }

        copy.setIntakes(copiedIntakes);

        return prescriptionRepository.save(copy);
    }

    @Override
    public Page<PrescriptionProjection> searchByName(String name, Pageable pageable) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return prescriptionRepository.findByUser_IdAndNameContainingIgnoreCase(userId, name, pageable);
    }

    @Override
    public Page<PrescriptionProjection> searchByDate(LocalDate start, LocalDate end, Pageable pageable) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return prescriptionRepository
                .findByUser_IdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(userId, start, end, pageable);
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

    @Override
    public Prescription getById(String prescriptionId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return prescriptionRepository.findByUser_IdAndId(userId,prescriptionId);
    }

    @Override
    public Intake updateIntakeById(String id){
        Intake intake = intakeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.NOT_FOUND)
        );
        intake.setStatus(true);
        intakeRepository.save(intake);
        return intake;
    }

}
