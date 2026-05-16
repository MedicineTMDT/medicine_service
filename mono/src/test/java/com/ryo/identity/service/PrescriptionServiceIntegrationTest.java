package com.ryo.identity.service;

import com.ryo.identity.constant.*;
import com.ryo.identity.dto.MedicationSchedule;
import com.ryo.identity.dto.request.CreatePrescriptionRequest;
import com.ryo.identity.dto.request.IntakeRequest;
import com.ryo.identity.dto.response.PrescriptionInfo;
import com.ryo.identity.entity.*;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.projection.PrescriptionProjection;
import com.ryo.identity.repository.*;
import com.ryo.identity.service.impl.PrescriptionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PrescriptionServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private PrescriptionServiceImpl prescriptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private MergedIngredientRepository mergedIngredientRepository;

    @Autowired
    private DrugInteractionRepository drugInteractionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createPrescription_asUser_shouldActivateAndCreateIntakes() {
        User patient = saveUser("patient10", "patient10@example.com", Role.USER);
        authenticateAs(patient.getId(), "USER");

        Prescription result = prescriptionService.createPrescription(prescriptionRequest(
                "Flu prescription",
                LocalDate.of(2026, 5, 1),
                List.of(intakeRequest("Paracetamol", 3,
                        List.of(new MedicationSchedule(Timing.MORNING, 1),
                                new MedicationSchedule(Timing.EVENING, 1))))
        ));

        assertNotNull(result.getId());
        assertTrue(result.getActivate());
        assertEquals(patient.getId(), result.getPatient().getId());
        assertEquals(2, result.getIntakes().size());
        assertEquals(LocalDate.of(2026, 5, 2), result.getEndDate());
    }

    @Test
    void createPrescription_whenDoseSumGreaterThanTotal_shouldThrowAppException() {
        User patient = saveUser("patient11", "patient11@example.com", Role.USER);
        authenticateAs(patient.getId(), "USER");

        assertThrows(AppException.class, () -> prescriptionService.createPrescription(prescriptionRequest(
                "Invalid",
                LocalDate.of(2026, 5, 1),
                List.of(intakeRequest("Paracetamol", 1,
                        List.of(new MedicationSchedule(Timing.MORNING, 2))))
        )));
    }

    @Test
    void createPrescription_asDoctorWithPatientEmail_shouldCreateInactivePrescriptionForPatient() {
        User doctor = saveUser("doctor10", "doctor10@example.com", Role.MED);
        User patient = saveUser("patient12", "patient12@example.com", Role.USER);
        authenticateAs(doctor.getId(), "MED");

        Prescription result = prescriptionService.createPrescription(new CreatePrescriptionRequest(
                "Doctor prescription",
                "Description",
                null,
                patient.getEmail(),
                LocalDate.of(2026, 5, 1),
                "Message",
                "Diagnosis",
                Map.of("source", "doctor"),
                List.of(intakeRequest("Aspirin", 2,
                        List.of(new MedicationSchedule(Timing.MORNING, 1)))),
                null
        ));

        assertFalse(result.getActivate());
        assertEquals(doctor.getId(), result.getUser().getId());
        assertEquals(patient.getId(), result.getPatient().getId());
    }

    @Test
    void searchByName_asUser_shouldReturnOnlyActivePatientPrescriptions() {
        User patient = saveUser("patient13", "patient13@example.com", Role.USER);
        User doctor = saveUser("doctor13", "doctor13@example.com", Role.MED);
        savePrescription("Heart plan", patient, doctor, true, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));
        savePrescription("Heart inactive", patient, doctor, false, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));
        authenticateAs(patient.getId(), "USER");

        Page<PrescriptionProjection> result =
                prescriptionService.searchByName("heart", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Heart plan", result.getContent().getFirst().getName());
    }

    @Test
    void getById_asDoctor_shouldReturnOwnedPrescription() {
        User doctor = saveUser("doctor14", "doctor14@example.com", Role.MED);
        User patient = saveUser("patient14", "patient14@example.com", Role.USER);
        Prescription prescription =
                savePrescription("Owned", patient, doctor, false, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));
        authenticateAs(doctor.getId(), "MED");

        Prescription result = prescriptionService.getById(prescription.getId());

        assertEquals("Owned", result.getName());
    }

    @Test
    void updateIntakeById_shouldMarkIntakeDone() {
        User patient = saveUser("patient15", "patient15@example.com", Role.USER);
        Prescription prescription =
                savePrescription("With intake", patient, patient, true, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 1));
        Intake intake = intakeRepository.save(Intake.builder()
                .prescription(prescription)
                .time(LocalDateTime.of(2026, 5, 1, 6, 30))
                .status(false)
                .info(List.of(Map.of("drugName", "Paracetamol")))
                .build());

        Intake result = prescriptionService.updateIntakeById(intake.getId());

        assertTrue(result.getStatus());
    }

    @Test
    void acceptPrescription_shouldActivateForCurrentUser() {
        User doctor = saveUser("doctor16", "doctor16@example.com", Role.MED);
        User patient = saveUser("patient16", "patient16@example.com", Role.USER);
        Prescription prescription =
                savePrescription("Pending", null, doctor, false, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 2));
        authenticateAs(patient.getId(), "USER");

        prescriptionService.accept_prescription(prescription.getId());

        Prescription updated = prescriptionRepository.findById(prescription.getId()).orElseThrow();
        assertTrue(updated.getActivate());
        assertEquals(patient.getId(), updated.getPatient().getId());
    }

    @Test
    void copyPrescription_shouldCloneOriginalForCurrentUser() {
        User doctor = saveUser("doctor17", "doctor17@example.com", Role.MED);
        User patient = saveUser("patient17", "patient17@example.com", Role.USER);
        Prescription original =
                savePrescription("Original", null, doctor, false, LocalDate.now().minusDays(2), LocalDate.now().plusDays(2));
        Intake intake = Intake.builder()
                .prescription(original)
                .time(LocalDateTime.now().minusDays(2))
                .status(true)
                .info(List.of(Map.of("drugName", "Paracetamol")))
                .build();
        original.setIntakes(new ArrayList<>(List.of(intake)));
        original = prescriptionRepository.save(original);
        authenticateAs(patient.getId(), "USER");

        Prescription copy = prescriptionService.copyPrescription(original.getId());

        assertNotEquals(original.getId(), copy.getId());
        assertTrue(copy.getActivate());
        assertEquals(patient.getId(), copy.getPatient().getId());
        assertEquals(original.getId(), copy.getOrgPrescriptionId());
        assertFalse(copy.getIntakes().getFirst().getStatus());
    }

    @Test
    void getPrescriptionReview_shouldReturnDrugInfoAndInteractions() {
        MergedIngredient paracetamol = mergedIngredientRepository.save(
                MergedIngredient.builder().name("Paracetamol").build());
        MergedIngredient warfarin = mergedIngredientRepository.save(
                MergedIngredient.builder().name("Warfarin").build());
        Drug drug1 = drugRepository.save(Drug.builder()
                .name("Drug A")
                .slug("drug-a")
                .info(Map.of("warning", "none"))
                .mergedIngredients(Set.of(paracetamol, warfarin))
                .build());
        drugInteractionRepository.save(DrugInteraction.builder()
                .hoatChat1Name("Paracetamol")
                .hoatChat2Name("Warfarin")
                .mucDoNghiemTrong("High")
                .hauQuaCuaTuongTac("Bleeding")
                .build());

        PrescriptionInfo result = prescriptionService.getPrescriptionReview(List.of(drug1.getId()));

        assertEquals(1, result.getInfo().size());
        assertEquals(1, result.getDrugInteractionResponseList().size());
        assertEquals("High", result.getDrugInteractionResponseList().getFirst().get("mucDoNghiemTrong"));
    }

    @Test
    void doctorDeletePrescription_shouldDeleteOwnedPrescription() {
        User doctor = saveUser("doctor18", "doctor18@example.com", Role.MED);
        Prescription prescription =
                savePrescription("Delete doctor", null, doctor, false, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 2));
        authenticateAs(doctor.getId(), "MED");

        prescriptionService.doctor_delete_prescription(prescription.getId());

        assertFalse(prescriptionRepository.existsById(prescription.getId()));
    }

    private CreatePrescriptionRequest prescriptionRequest(
            String name,
            LocalDate startDate,
            List<IntakeRequest> intakes
    ) {
        return new CreatePrescriptionRequest(
                name,
                "Description",
                null,
                null,
                startDate,
                "Message",
                "Diagnosis",
                Map.of("note", "test"),
                intakes,
                null
        );
    }

    private IntakeRequest intakeRequest(
            String drugName,
            int total,
            List<MedicationSchedule> timingList
    ) {
        return new IntakeRequest(
                drugName,
                null,
                total,
                DosageUnit.MG,
                500,
                MedicineForm.TABLET,
                Usage.ORAL,
                timingList,
                List.of(Note.AFTER_MEAL)
        );
    }

    private Prescription savePrescription(
            String name,
            User patient,
            User doctor,
            boolean active,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return prescriptionRepository.save(Prescription.builder()
                .name(name)
                .description("Description")
                .startDate(startDate)
                .endDate(endDate)
                .message("Message")
                .diagnosisNote("Diagnosis")
                .info(new HashMap<>())
                .user(doctor)
                .patient(patient)
                .activate(active)
                .orgPrescriptionId("")
                .intakes(new ArrayList<>())
                .build());
    }

    private User saveUser(String username, String email, Role role) {
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .firstName("First")
                .lastName("Last")
                .email(email)
                .avatarImg("")
                .role(role)
                .verifyEmail(true)
                .build());
    }
}
