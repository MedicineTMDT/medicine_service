package com.ryo.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryo.request.dto.request.DrugInteractionRequest;
import com.ryo.request.entity.DrugInteraction;
import com.ryo.request.service.IDrugInteractionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Slf4j
@WebMvcTest(DrugInteractionController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties")
public class DrugInteractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDrugInteractionService interactionService;

    private ObjectMapper objectMapper;

    private DrugInteractionRequest interactionRequest;
    private DrugInteraction interactionResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        interactionRequest = new DrugInteractionRequest();
        interactionRequest.setMucDoNghiemTrong("Nghiêm trọng");
        interactionRequest.setHauQuaCuaTuongTac("Tăng nguy cơ chảy máu");
        interactionRequest.setCoCheTuongTac("Ức chế chuyển hóa CYP2C9");
        interactionRequest.setXuTriTuongTac("Giảm liều hoặc thay thế thuốc");
        interactionRequest.setHoatChat1Name("Warfarin");
        interactionRequest.setHoatChat2Name("Aspirin");
        interactionRequest.setIngredient1Id(1);
        interactionRequest.setIngredient2Id(2);

        interactionResponse = new DrugInteraction();
        interactionResponse.setId(1);
        interactionResponse.setMucDoNghiemTrong("Nghiêm trọng");
        interactionResponse.setHauQuaCuaTuongTac("Tăng nguy cơ chảy máu");
        interactionResponse.setCoCheTuongTac("Ức chế chuyển hóa CYP2C9");
        interactionResponse.setXuTriTuongTac("Giảm liều hoặc thay thế thuốc");
        interactionResponse.setHoatChat1Name("Warfarin");
        interactionResponse.setHoatChat2Name("Aspirin");
    }

    // ════════════════════════════════════════════════════════════
    //  POST /drug-interactions  — CREATE
    // ════════════════════════════════════════════════════════════

    @Test
    void create_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(interactionRequest);
        Mockito.when(interactionService.create(ArgumentMatchers.any()))
                .thenReturn(interactionResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/drug-interactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.mucDoNghiemTrong")
                        .value("Nghiêm trọng"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.hoatChat1Name")
                        .value("Warfarin"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.hoatChat2Name")
                        .value("Aspirin"));
    }

    @Test
    void create_emptyBody_stillCallsService() throws Exception {
        // Given — không có @Valid nên body rỗng vẫn qua controller
        Mockito.when(interactionService.create(ArgumentMatchers.any()))
                .thenReturn(new DrugInteraction());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/drug-interactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000));
    }

    @Test
    void create_serviceThrowsException_returnsError() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(interactionRequest);
        Mockito.when(interactionService.create(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Interaction already exists"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/drug-interactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  PUT /drug-interactions/{id}  — UPDATE
    // ════════════════════════════════════════════════════════════

    @Test
    void update_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(interactionRequest);
        Mockito.when(interactionService.update(ArgumentMatchers.any(), eq(1)))
                .thenReturn(interactionResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/drug-interactions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.mucDoNghiemTrong")
                        .value("Nghiêm trọng"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.hoatChat1Name")
                        .value("Warfarin"));
    }

    @Test
    void update_notFound_returnsError() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(interactionRequest);
        Mockito.when(interactionService.update(ArgumentMatchers.any(), eq(999)))
                .thenThrow(new RuntimeException("DrugInteraction not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/drug-interactions/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void update_invalidIdType_returnsBadRequest() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(interactionRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/drug-interactions/abc")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  DELETE /drug-interactions/{id}  — DELETE
    // ════════════════════════════════════════════════════════════

    @Test
    void delete_happyPath() throws Exception {
        // Given
        doNothing().when(interactionService).delete(1);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/drug-interactions/1")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Deleted successfully"));
    }

    @Test
    void delete_notFound_returnsError() throws Exception {
        // Given
        doThrow(new RuntimeException("DrugInteraction not found"))
                .when(interactionService).delete(999);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/drug-interactions/999")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void delete_invalidIdType_returnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/drug-interactions/abc")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /drug-interactions/{id}  — GET ONE
    // ════════════════════════════════════════════════════════════

    @Test
    void get_happyPath() throws Exception {
        // Given
        Mockito.when(interactionService.get(1)).thenReturn(interactionResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drug-interactions/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.mucDoNghiemTrong")
                        .value("Nghiêm trọng"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.coCheTuongTac")
                        .value("Ức chế chuyển hóa CYP2C9"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.xuTriTuongTac")
                        .value("Giảm liều hoặc thay thế thuốc"));
    }

    @Test
    void get_notFound_returnsError() throws Exception {
        // Given
        Mockito.when(interactionService.get(999))
                .thenThrow(new RuntimeException("DrugInteraction not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drug-interactions/999"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void get_invalidIdType_returnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drug-interactions/abc"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /drug-interactions/search-by-ingredients
    // ════════════════════════════════════════════════════════════

    @Test
    void searchByIngredients_happyPath() throws Exception {
        // Given
        List<DrugInteraction> interactions = List.of(interactionResponse);
        Mockito.when(interactionService.getByListIngredientName(
                        List.of("Warfarin", "Aspirin")))
                .thenReturn(interactions);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drug-interactions/search-by-ingredients")
                        .param("ingredientNames", "Warfarin", "Aspirin"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result[0].hoatChat1Name")
                        .value("Warfarin"))
                .andExpect(MockMvcResultMatchers.jsonPath("result[0].hoatChat2Name")
                        .value("Aspirin"))
                .andExpect(MockMvcResultMatchers.jsonPath("result[0].mucDoNghiemTrong")
                        .value("Nghiêm trọng"));
    }

    @Test
    void searchByIngredients_singleIngredient() throws Exception {
        // Given — tra cứu với 1 hoạt chất duy nhất
        List<DrugInteraction> interactions = List.of(interactionResponse);
        Mockito.when(interactionService.getByListIngredientName(List.of("Warfarin")))
                .thenReturn(interactions);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drug-interactions/search-by-ingredients")
                        .param("ingredientNames", "Warfarin"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.length()").value(1));
    }

    @Test
    void searchByIngredients_noMatch_returnsEmptyList() throws Exception {
        // Given — không có tương tác nào với các hoạt chất này
        Mockito.when(interactionService.getByListIngredientName(
                        List.of("Vitamin C", "Zinc")))
                .thenReturn(List.of());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drug-interactions/search-by-ingredients")
                        .param("ingredientNames", "Vitamin C", "Zinc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result").isEmpty());
    }

    @Test
    void searchByIngredients_multipleResults() throws Exception {
        // Given — nhiều cặp tương tác cùng lúc
        DrugInteraction second = new DrugInteraction();
        second.setId(2);
        second.setHoatChat1Name("Warfarin");
        second.setHoatChat2Name("Ibuprofen");
        second.setMucDoNghiemTrong("Trung bình");

        Mockito.when(interactionService.getByListIngredientName(
                        List.of("Warfarin", "Aspirin", "Ibuprofen")))
                .thenReturn(List.of(interactionResponse, second));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drug-interactions/search-by-ingredients")
                        .param("ingredientNames", "Warfarin", "Aspirin", "Ibuprofen"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("result[1].hoatChat2Name")
                        .value("Ibuprofen"))
                .andExpect(MockMvcResultMatchers.jsonPath("result[1].mucDoNghiemTrong")
                        .value("Trung bình"));
    }

    @Test
    void searchByIngredients_missingParam_returnsBadRequest() throws Exception {
        // Given — thiếu @RequestParam ingredientNames bắt buộc
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drug-interactions/search-by-ingredients"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void searchByIngredients_serviceThrowsException_returnsError() throws Exception {
        // Given
        Mockito.when(interactionService.getByListIngredientName(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Database error"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drug-interactions/search-by-ingredients")
                        .param("ingredientNames", "Warfarin"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}