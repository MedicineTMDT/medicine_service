package com.ryo.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryo.identity.constant.*;
import com.ryo.identity.dto.MedicationSchedule;
import com.ryo.identity.dto.request.CreatePrescriptionRequest;
import com.ryo.identity.dto.request.IntakeRequest;
import com.ryo.identity.dto.response.PrescriptionInfo;
import com.ryo.identity.entity.Intake;
import com.ryo.identity.entity.Prescription;
import com.ryo.identity.projection.PrescriptionProjection;
import com.ryo.identity.service.IPrescriptionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Slf4j
@WebMvcTest(PrescriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties")
public class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IPrescriptionService prescriptionService;

    private ObjectMapper objectMapper;

    // ── Entities ─────────────────────────────────────────────────
    private Prescription prescription;
    private Intake intake;

    // ── DTOs ─────────────────────────────────────────────────────
    private CreatePrescriptionRequest createRequest;
    private PrescriptionInfo prescriptionInfo;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // IntakeRequest
        MedicationSchedule morning = new MedicationSchedule(Timing.MORNING, 1);
        IntakeRequest intakeRequest = new IntakeRequest(
                "Paracetamol 500mg",
                "drug-001",
                7,
                DosageUnit.MG,
                500,
                MedicineForm.TABLET,
                Usage.ORAL,
                List.of(morning),
                List.of(Note.AFTER_MEAL)
        );

        // CreatePrescriptionRequest
        createRequest = new CreatePrescriptionRequest(
                "Đơn thuốc cảm cúm",
                "Điều trị cảm cúm thông thường",
                "user-001",
                "patient@gmail.com",
                LocalDate.of(2025, 1, 1),
                "Uống đủ liều",
                "Viêm hô hấp trên",
                Map.of("contraindication", "Không dùng cho trẻ dưới 2 tuổi"),
                List.of(intakeRequest)
        );

        // Prescription entity
        prescription = new Prescription();
        prescription.setId("prescription-001");
        prescription.setName("Đơn thuốc cảm cúm");

        // Intake entity
        intake = new Intake();
        intake.setId("intake-001");

        // PrescriptionInfo
        prescriptionInfo = PrescriptionInfo.builder()
                .info(List.of(Map.of(
                        "Paracetamol",
                        Map.of("warning", "Không dùng quá 4g/ngày")
                )))
                .drugInteractionResponseList(List.of(Map.of(
                        "drug1", "Warfarin",
                        "drug2", "Aspirin",
                        "severity", "Nghiêm trọng"
                )))
                .build();
    }

    // ════════════════════════════════════════════════════════════
    //  POST /prescriptions/scan  — SCAN IMAGE
    // ════════════════════════════════════════════════════════════

    @Test
    void scan_happyPath() throws Exception {
        // Given
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "prescription.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-bytes".getBytes()
        );
        Mockito.when(prescriptionService.extractPrescriptionFromImage(
                        anyString(), eq(MediaType.IMAGE_JPEG_VALUE)))
                .thenReturn(createRequest);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/prescriptions/scan")
                        .file(image)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("name")
                        .value("Đơn thuốc cảm cúm"))
                .andExpect(MockMvcResultMatchers.jsonPath("patientEmailAddress")
                        .value("patient@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("intakes[0].drugName")
                        .value("Paracetamol 500mg"));
    }

    @Test
    void scan_pngImage_happyPath() throws Exception {
        // Given — ảnh PNG thay vì JPEG
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "prescription.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-png-bytes".getBytes()
        );
        Mockito.when(prescriptionService.extractPrescriptionFromImage(
                        anyString(), eq(MediaType.IMAGE_PNG_VALUE)))
                .thenReturn(createRequest);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/prescriptions/scan")
                        .file(image)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void scan_missingFile_returnsBadRequest() throws Exception {
        // Given — không đính kèm file
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/prescriptions/scan")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void scan_serviceThrowsException_returnsError() throws Exception {
        // Given — AI service lỗi
        MockMultipartFile image = new MockMultipartFile(
                "image", "prescription.jpg",
                MediaType.IMAGE_JPEG_VALUE, "bytes".getBytes()
        );
        Mockito.when(prescriptionService.extractPrescriptionFromImage(anyString(), anyString()))
                .thenThrow(new RuntimeException("AI service unavailable"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/prescriptions/scan")
                        .file(image)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  POST /prescriptions  — CREATE
    // ════════════════════════════════════════════════════════════

    @Test
    void create_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(createRequest);
        Mockito.when(prescriptionService.createPrescription(ArgumentMatchers.any()))
                .thenReturn(prescription);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/prescriptions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("prescription-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("Đơn thuốc cảm cúm"));
    }

    @Test
    void create_emptyBody_stillCallsService() throws Exception {
        // Given — không có @Valid nên body rỗng vẫn qua
        Mockito.when(prescriptionService.createPrescription(ArgumentMatchers.any()))
                .thenReturn(new Prescription());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/prescriptions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void create_serviceThrowsException_returnsError() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(createRequest);
        Mockito.when(prescriptionService.createPrescription(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Patient not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/prescriptions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  POST /prescriptions/{id}/copy  — COPY
    // ════════════════════════════════════════════════════════════

    @Test
    void copy_happyPath() throws Exception {
        // Given
        Prescription copied = new Prescription();
        copied.setId("prescription-copy-001");
        copied.setName("Đơn thuốc cảm cúm");
        Mockito.when(prescriptionService.copyPrescription("prescription-001"))
                .thenReturn(copied);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/prescriptions/prescription-001/copy")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id")
                        .value("prescription-copy-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("name")
                        .value("Đơn thuốc cảm cúm"));
    }

    @Test
    void copy_notFound_returnsError() throws Exception {
        // Given
        Mockito.when(prescriptionService.copyPrescription("not-exist"))
                .thenThrow(new RuntimeException("Prescription not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/prescriptions/not-exist/copy")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /prescriptions/search/name  — SEARCH BY NAME
    // ════════════════════════════════════════════════════════════

//    @Test
//    void searchByName_happyPath() throws Exception {
//        // Given
//        PrescriptionProjection projection = Mockito.mock(PrescriptionProjection.class);
//        Mockito.when(projection.getId()).thenReturn("prescription-001");
//        Mockito.when(projection.getName()).thenReturn("Đơn thuốc cảm cúm");
//        Mockito.when(projection.getDescription()).thenReturn("Đơn thuốc cảm cúm");
////        Mockito.when(projection.getStartDate()).thenReturn();
////        Mockito.when(projection.getEndDate()).thenReturn();
//
//        Page<PrescriptionProjection> page = new PageImpl<>(
//                List.of(projection), PageRequest.of(0, 10), 1
//        );
//        Mockito.when(prescriptionService.searchByName(eq("cảm cúm"), ArgumentMatchers.any()))
//                .thenReturn(page);
//
//        // When Then
//        mockMvc.perform(MockMvcRequestBuilders
//                        .get("/prescriptions/search/name")
//                        .param("name", "cảm cúm")
//                        .param("page", "0")
//                        .param("size", "10"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("content[0].id")
//                        .value("prescription-001"))
//                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1));
//    }

    @Test
    void searchByName_noMatch_returnsEmptyPage() throws Exception {
        // Given
        Page<PrescriptionProjection> emptyPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 10), 0
        );
        Mockito.when(prescriptionService.searchByName(eq("xyz"), ArgumentMatchers.any()))
                .thenReturn(emptyPage);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/search/name")
                        .param("name", "xyz"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content").isEmpty());
    }

    @Test
    void searchByName_missingParam_returnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/search/name"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /prescriptions/search/date  — SEARCH BY DATE
    // ════════════════════════════════════════════════════════════

//    @Test
//    void searchByDate_happyPath() throws Exception {
//        // Given
//        PrescriptionProjection projection = Mockito.mock(PrescriptionProjection.class);
//        Mockito.when(projection.getId()).thenReturn("prescription-001");
//
//        Page<PrescriptionProjection> page = new PageImpl<>(
//                List.of(projection), PageRequest.of(0, 10), 1
//        );
//        Mockito.when(prescriptionService.searchByDate(
//                        eq(LocalDate.of(2025, 1, 1)),
//                        eq(LocalDate.of(2025, 1, 31)),
//                        ArgumentMatchers.any()))
//                .thenReturn(page);
//
//        // When Then
//        mockMvc.perform(MockMvcRequestBuilders
//                        .get("/prescriptions/search/date")
//                        .param("start", "2025-01-01")
//                        .param("end", "2025-01-31")
//                        .param("page", "0")
//                        .param("size", "10"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("content[0].id")
//                        .value("prescription-001"))
//                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1));
//    }

    @Test
    void searchByDate_noMatch_returnsEmptyPage() throws Exception {
        // Given
        Page<PrescriptionProjection> emptyPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 10), 0
        );
        Mockito.when(prescriptionService.searchByDate(
                        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(emptyPage);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/search/date")
                        .param("start", "2000-01-01")
                        .param("end", "2000-01-02"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content").isEmpty());
    }

    @Test
    void searchByDate_missingStartParam_returnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/search/date")
                        .param("end", "2025-01-31"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void searchByDate_missingEndParam_returnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/search/date")
                        .param("start", "2025-01-01"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void searchByDate_invalidDateFormat_returnsBadRequest() throws Exception {
        // Given — ngày sai định dạng
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/search/date")
                        .param("start", "01-01-2025")
                        .param("end", "31-01-2025"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /prescriptions/review  — DRUG INTERACTION REVIEW
    // ════════════════════════════════════════════════════════════

    @Test
    void review_happyPath() throws Exception {
        // Given
        Mockito.when(prescriptionService.getPrescriptionReview(List.of(1, 2, 3)))
                .thenReturn(prescriptionInfo);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/review")
                        .param("listDrugIds", "1", "2", "3"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                                "drugInteractionResponseList[0].severity")
                        .value("Nghiêm trọng"))
                .andExpect(MockMvcResultMatchers.jsonPath(
                                "drugInteractionResponseList[0].drug1")
                        .value("Warfarin"));
    }

    @Test
    void review_singleDrug_noInteraction() throws Exception {
        // Given — 1 thuốc không thể có tương tác
        PrescriptionInfo noInteraction = PrescriptionInfo.builder()
                .info(List.of())
                .drugInteractionResponseList(List.of())
                .build();
        Mockito.when(prescriptionService.getPrescriptionReview(List.of(1)))
                .thenReturn(noInteraction);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/review")
                        .param("listDrugIds", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "drugInteractionResponseList").isEmpty());
    }

    @Test
    void review_missingParam_returnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/review"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void review_serviceThrowsException_returnsError() throws Exception {
        // Given
        Mockito.when(prescriptionService.getPrescriptionReview(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Drug not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/review")
                        .param("listDrugIds", "999"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  PUT /prescriptions/edit/{id}  — EDIT INTAKE STATUS
    // ════════════════════════════════════════════════════════════

    @Test
    void editIntakeStatus_happyPath() throws Exception {
        // Given
        Mockito.when(prescriptionService.updateIntakeById("intake-001"))
                .thenReturn(intake);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/prescriptions/edit/intake-001")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("intake-001"));
    }

    @Test
    void editIntakeStatus_notFound_returnsError() throws Exception {
        // Given
        Mockito.when(prescriptionService.updateIntakeById("not-exist"))
                .thenThrow(new RuntimeException("Intake not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/prescriptions/edit/not-exist")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /prescriptions/{id}  — GET BY ID
    // ════════════════════════════════════════════════════════════

    @Test
    void getById_happyPath() throws Exception {
        // Given
        Mockito.when(prescriptionService.getById("prescription-001"))
                .thenReturn(prescription);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/prescription-001"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("prescription-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("Đơn thuốc cảm cúm"));
    }

    @Test
    void getById_notFound_returnsError() throws Exception {
        // Given
        Mockito.when(prescriptionService.getById("not-exist"))
                .thenThrow(new RuntimeException("Prescription not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/prescriptions/not-exist"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  PUT /prescriptions/{id}/accept  — ACCEPT (PATIENT)
    // ════════════════════════════════════════════════════════════

    @Test
    void accept_happyPath() throws Exception {
        // Given
        doNothing().when(prescriptionService).accept_prescription("prescription-001");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/prescriptions/prescription-001/accept")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void accept_alreadyAccepted_returnsError() throws Exception {
        // Given
        doThrow(new RuntimeException("Prescription already accepted"))
                .when(prescriptionService).accept_prescription("prescription-001");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/prescriptions/prescription-001/accept")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void accept_notFound_returnsError() throws Exception {
        // Given
        doThrow(new RuntimeException("Prescription not found"))
                .when(prescriptionService).accept_prescription("not-exist");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/prescriptions/not-exist/accept")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  DELETE /prescriptions/{id}/doctor  — DOCTOR DELETE
    // ════════════════════════════════════════════════════════════

    @Test
    void doctorDelete_happyPath() throws Exception {
        // Given
        doNothing().when(prescriptionService)
                .doctor_delete_prescription("prescription-001");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/prescriptions/prescription-001/doctor")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void doctorDelete_notFound_returnsError() throws Exception {
        // Given
        doThrow(new RuntimeException("Prescription not found"))
                .when(prescriptionService).doctor_delete_prescription("not-exist");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/prescriptions/not-exist/doctor")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void doctorDelete_unauthorized_returnsError() throws Exception {
        // Given — bác sĩ không có quyền xóa đơn của người khác
        doThrow(new RuntimeException("Unauthorized"))
                .when(prescriptionService).doctor_delete_prescription("prescription-002");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/prescriptions/prescription-002/doctor")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  DELETE /prescriptions/{id}/patient  — PATIENT DELETE
    // ════════════════════════════════════════════════════════════

    @Test
    void patientDelete_happyPath() throws Exception {
        // Given
        doNothing().when(prescriptionService)
                .user_delete_prescription("prescription-001");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/prescriptions/prescription-001/patient")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void patientDelete_notFound_returnsError() throws Exception {
        // Given
        doThrow(new RuntimeException("Prescription not found"))
                .when(prescriptionService).user_delete_prescription("not-exist");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/prescriptions/not-exist/patient")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void patientDelete_unauthorized_returnsError() throws Exception {
        // Given — bệnh nhân không có quyền xóa đơn của người khác
        doThrow(new RuntimeException("Unauthorized"))
                .when(prescriptionService).user_delete_prescription("prescription-002");

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/prescriptions/prescription-002/patient")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}