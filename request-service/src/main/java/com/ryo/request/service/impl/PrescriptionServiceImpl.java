package com.ryo.request.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryo.request.dto.MedicationSchedule;
import com.ryo.request.entity.*;
import com.ryo.request.exception.AppException;
import com.ryo.request.exception.ErrorCode;
import com.ryo.request.projection.PrescriptionProjection;
import com.ryo.request.repository.DrugRepository;
import com.ryo.request.repository.IntakeRepository;
import com.ryo.request.repository.UserRepository;
import com.ryo.request.service.IPrescriptionService;
import com.ryo.request.dto.request.CreatePrescriptionRequest;
import com.ryo.request.dto.request.IntakeRequest;
import com.ryo.request.dto.response.PrescriptionInfo;
import com.ryo.request.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements IPrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final DrugRepository drugRepository;
    private final DrugInteractionServiceImpl drugInteractionService;
    private final IntakeRepository intakeRepository;
    private final EmailService emailService;

    private final RestClient geminiRestClient;
    private final String geminiModel;

    public CreatePrescriptionRequest extractPrescriptionFromImage(String base64Image, String mimeType) {
        log.info("extractPrescriptionFromImage");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaim("email");

        String systemPrompt = """
        Bạn là AI chuyên đọc đơn thuốc từ hình ảnh.
        Nhiệm vụ: Trích xuất thông tin đơn thuốc từ hình ảnh và trả về JSON.
        
        Yêu cầu:
        - Chỉ trả về JSON thuần túy, không markdown, không giải thích
        - Nếu không đọc được thì trả về null
        
        Các enum hợp lệ:
        
        Usage (đường dùng):
          ORAL, SUBLINGUAL, CHEW, TOPICAL, EYE_DROPS, EAR_DROPS, NASAL_DROPS, IM, IV, SC, RECTAL, VAGINAL
        
        MedicineForm (dạng thuốc):
          TABLET (Viên), POWDER (Gói/Bột), VIAL (Ống), SYRUP (Lọ/Siro), TUBE (Tuýp), BOTTLE (Chai)
        
        DosageUnit (đơn vị liều):
          MG, MCG, G, IU, ML, PERCENT
        
        Timing (thời điểm dùng thuốc):
          MORNING, NOON, AFTERNOON, EVENING, BEDTIME
        
        Note (ghi chú uống thuốc):
          BEFORE_MEAL, AFTER_MEAL, WITH_MEAL, WITH_WATER, AVOID_ALCOHOL, AVOID_DAIRY
        
        Format output (chỉ JSON, không markdown):
        {
          "name": "Tên đơn thuốc hoặc tên bệnh nhân nếu có",
          "description": "Mô tả ngắn đơn thuốc nếu có, hoặc null",
          "userId": null,
          "patientEmailAddress": "%s",
          "startDate": "YYYY-MM-DD hoặc null nếu không có",
          "message": "Lời nhắn từ bác sĩ nếu có, hoặc null",
          "diagnosisNote": "Chẩn đoán bệnh nếu có, hoặc null",
          "info": null,
          "intakes": [
            {
              "drugName": "Tên thuốc",
              "drugId": null,
              "total": 10,
              "unit": "MG",
              "quantitative": 500,
              "medicineForm": "TABLET",
              "usage": "ORAL",
              "timingList": [
                { "timing": "MORNING", "quantity": 1 },
                { "timing": "EVENING", "quantity": 1 }
              ],
              "noteList": ["BEFORE_MEAL"]
            }
          ]
        }
        """.formatted(email);

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of(
                                        "inline_data", Map.of(
                                                "mime_type", mimeType,
                                                "data", base64Image
                                        )
                                ),
                                Map.of("text", "Hãy đọc đơn thuốc trong hình và trả về JSON theo đúng format yêu cầu.")
                        ))
                )
        );

        try {
            Map response = geminiRestClient.post()
                    .uri("/{model}:generateContent", geminiModel)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<Map> candidates = (List<Map>) response.get("candidates");
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            String rawText = (String) parts.get(0).get("text");

            rawText = rawText.trim()
                    .replaceAll("(?s)```json", "")
                    .replaceAll("```", "")
                    .trim();

            if (rawText.equalsIgnoreCase("null") || rawText.isEmpty()) {
                log.warn("Gemini returned null or empty response");
                return null;
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(rawText, CreatePrescriptionRequest.class);
        } catch (Exception e) {
            log.error("Gemini scan error: {}", e.getMessage());
            return null;
        }
    }

    public String ask(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            Map response = geminiRestClient.post()
                    .uri("/{model}:generateContent", geminiModel)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<Map> candidates = (List<Map>) response.get("candidates");
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            log.error("Gemini error: {}", e.getMessage());
            throw new RuntimeException("AI service error: " + e.getMessage());
        }
    }

    // Có system prompt
    public String askWithSystem(String systemPrompt, String userMessage) {
        Map<String, Object> body = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", userMessage)
                        ))
                )
        );

        Map response = geminiRestClient.post()
                .uri("/{model}:generateContent", geminiModel)
                .body(body)
                .retrieve()
                .body(Map.class);

        List<Map> candidates = (List<Map>) response.get("candidates");
        Map content = (Map) candidates.get(0).get("content");
        List<Map> parts = (List<Map>) content.get("parts");
        return (String) parts.get(0).get("text");
    }

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
        
        for(IntakeRequest intakeRequest: request.intakes()){
            int total = intakeRequest.total();
            LocalDate drugStartDate = request.startDate(); // Each drug starts from original start date
            
            while(total > 0){
                boolean anyDoseGiven = false;
                
                // Process all timings for the current day
                for(MedicationSchedule medicationSchedule: intakeRequest.timingList()){
                    if(total - medicationSchedule.getQuantity() >= 0){
                        // Create key for seen map
                        LocalDateTime key = drugStartDate.atTime(medicationSchedule.getTiming().getTime());
                        if(key.isAfter(max)){
                            max = key;
                        }
                        Map<String, Object> innerMap = new HashMap<>();
                        innerMap.put("drugName", intakeRequest.drugName());
                        innerMap.put("drugId", intakeRequest.drugId());
                        innerMap.put("usage", intakeRequest.usage());
                        innerMap.put("medicineForm", intakeRequest.medicineForm());
                        innerMap.put("noteList", intakeRequest.noteList());
                        innerMap.put("quantitative", intakeRequest.quantitative());
                        innerMap.put("unit", intakeRequest.unit());
                        
                        if(seen.containsKey(key)){
                            seen.get(key).add(innerMap);
                        } else {
                            seen.put(key, new ArrayList<>(List.of(innerMap)));
                        }
                        
                        total -= medicationSchedule.getQuantity();
                        anyDoseGiven = true;
                    } else {
                        break;
                    }
                }
                
                // Only advance to next day after processing all timings for current day
                if(anyDoseGiven){
                    drugStartDate = drugStartDate.plusDays(1);
                } else {
                    break; // Safety exit if no doses could be given
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
        prescription.setActivate(false);
        prescription.setOrgPrescriptionId("");

        if(request.patientEmailAddress().isBlank() || request.patientEmailAddress().isEmpty()){
            return prescriptionRepository.save(prescription);
        }
        User patient = userRepository.findByEmail(request.patientEmailAddress())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        prescription.setPatient(patient);

        Prescription result = prescriptionRepository.save(prescription);
        emailService.sendPrescriptionConfirmationEmail
                (patient,user.getFirstName(),result.getId());

        return result;
    }

    @Override
    public Prescription copyPrescription(String prescriptionId) {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        Prescription check = prescriptionRepository
                .findByPatient_IdAndOrgPrescriptionIdAndActivateTrue(userId, prescriptionId);
        if (check != null && check.getEndDate().isAfter(LocalDate.now())){
            throw new AppException(ErrorCode.DUPLICATE_ACTIVE_PRESCRIPTION);
        }

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
                .user(original.getUser())
                .patient(user)
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
        copy.setActivate(true);
        copy.setOrgPrescriptionId(prescriptionId);
        return prescriptionRepository.save(copy);
    }

    @Override
    public Page<PrescriptionProjection> searchByName(String name, Pageable pageable) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("USER"));
        if(isUser){
            return prescriptionRepository.findByPatient_IdAndNameContainingIgnoreCaseAndActivateTrue(userId, name, pageable);
        }
        return prescriptionRepository.findByUser_IdAndNameContainingIgnoreCase(userId, name, pageable);
    }

    @Override
    public Page<PrescriptionProjection> searchByDate(LocalDate start, LocalDate end, Pageable pageable) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("USER"));
        if(isUser){
            return prescriptionRepository.
                    findByPatient_IdAndStartDateGreaterThanEqualAndEndDateLessThanEqualAndActivateTrue
                            (userId, start, end, pageable);
        }
        return prescriptionRepository
                .findByUser_IdAndStartDateGreaterThanEqualAndEndDateLessThanEqual
                        (userId, start, end, pageable);
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
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("USER"));
        if(isUser){
            return prescriptionRepository
                    .findByPatient_IdAndIdAndActivateTrue(userId,prescriptionId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));
        }
        return prescriptionRepository
                .findByUser_IdAndId(userId,prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));
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

    @Override
    public void accept_prescription(String prescriptionId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));
        prescription.setActivate(true);
        prescription.setPatient(user);
        prescriptionRepository.save(prescription);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('MED', 'ADMIN')")
    public void doctor_delete_prescription(String prescriptionId){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Prescription prescription = prescriptionRepository
                .findByUser_IdAndId(userId, prescriptionId)
                .orElseThrow(()-> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND))
                ;
        prescriptionRepository.delete(prescription);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('USER')")
    public void user_delete_prescription(String prescriptionId){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Prescription prescription = prescriptionRepository
                .findByPatient_IdAndId(userId, prescriptionId)
                .orElseThrow(()-> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND))
                ;
        prescriptionRepository.delete(prescription);
    }


}
