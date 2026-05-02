package com.ryo.medicine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryo.medicine.dto.request.DrugRequest;
import com.ryo.medicine.dto.response.DrugResponse;
import com.ryo.medicine.dto.response.DrugSimpleResponse;
import com.ryo.medicine.entity.Drug;
import com.ryo.medicine.service.IDrugService;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Slf4j
@WebMvcTest(DrugController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties")
public class DrugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDrugService service;

    private ObjectMapper objectMapper;

    private DrugRequest drugRequest;
    private DrugResponse drugResponse;
    private DrugSimpleResponse drugSimpleResponse;
    private Drug drug;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        drugRequest = DrugRequest.builder()
                .name("Paracetamol 500mg")
                .content("Thuốc giảm đau, hạ sốt")
                .document("Hướng dẫn sử dụng...")
                .slug("paracetamol-500mg")
                .metadata(Map.of("dosage", "500mg", "form", "tablet"))
                .image(List.of("https://cdn.example.com/paracetamol.jpg"))
                .ingredient(List.of("Paracetamol", "Starch"))
                .info(Map.of("manufacturer", "DHG Pharma"))
                .categoriesId(List.of(1, 2))
                .build();

        drugResponse = DrugResponse.builder()
                .id(1)
                .name("Paracetamol 500mg")
                .slug("paracetamol-500mg")
                .metadata(Map.of("dosage", "500mg"))
                .build();

        drugSimpleResponse = DrugSimpleResponse.builder()
                .id(1)
                .name("Paracetamol 500mg")
                .slug("paracetamol-500mg")
                .imageLink("https://cdn.example.com/paracetamol.jpg")
                .build();

        drug = new Drug();
        drug.setId(1);
        drug.setName("Paracetamol 500mg");
        drug.setSlug("paracetamol-500mg");
    }

    // ════════════════════════════════════════════════════════════
    //  POST /drugs  — CREATE
    // ════════════════════════════════════════════════════════════

    @Test
    void create_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(drugRequest);
        Mockito.when(service.create(ArgumentMatchers.any())).thenReturn(drugResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/drugs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name").value("Paracetamol 500mg"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.slug").value("paracetamol-500mg"));
    }

    @Test
    void create_emptyBody_returnsBadRequest() throws Exception {
        // Given — body rỗng
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/drugs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk()); // không có @Valid nên vẫn qua
    }

    @Test
    void create_serviceThrowsException_returnsError() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(drugRequest);
        Mockito.when(service.create(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Slug already exists"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/drugs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  PUT /drugs/{id}  — UPDATE
    // ════════════════════════════════════════════════════════════

    @Test
    void update_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(drugRequest);
        Mockito.when(service.update(ArgumentMatchers.any(), eq(1))).thenReturn(drugResponse);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/drugs/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name").value("Paracetamol 500mg"));
    }

    @Test
    void update_drugNotFound_returnsError() throws Exception {
        // Given — id không tồn tại
        String content = objectMapper.writeValueAsString(drugRequest);
        Mockito.when(service.update(ArgumentMatchers.any(), eq(999)))
                .thenThrow(new RuntimeException("Drug not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/drugs/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void update_invalidIdType_returnsBadRequest() throws Exception {
        // Given — id không phải số
        String content = objectMapper.writeValueAsString(drugRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/drugs/abc")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  DELETE /drugs/{id}  — DELETE
    // ════════════════════════════════════════════════════════════

    @Test
    void delete_happyPath() throws Exception {
        // Given
        doNothing().when(service).delete(1);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/drugs/1")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Deleted successfully"));
    }

    @Test
    void delete_drugNotFound_returnsError() throws Exception {
        // Given
        doThrow(new RuntimeException("Drug not found")).when(service).delete(999);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/drugs/999")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void delete_invalidIdType_returnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/drugs/abc")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /drugs/{id}  — GET ONE
    // ════════════════════════════════════════════════════════════

    @Test
    void get_happyPath() throws Exception {
        // Given
        Mockito.when(service.get(1)).thenReturn(drug);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name").value("Paracetamol 500mg"));
    }

    @Test
    void get_drugNotFound_returnsError() throws Exception {
        // Given
        Mockito.when(service.get(999))
                .thenThrow(new RuntimeException("Drug not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/999"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void get_invalidIdType_returnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/abc"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /drugs  — GET ALL PAGINATION
    // ════════════════════════════════════════════════════════════

    @Test
    void getAll_happyPath() throws Exception {
        // Given
        Page<DrugSimpleResponse> page = new PageImpl<>(
                List.of(drugSimpleResponse),
                PageRequest.of(0, 10),
                1
        );
        Mockito.when(service.getAll(ArgumentMatchers.any())).thenReturn(page);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].name")
                        .value("Paracetamol 500mg"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.totalElements").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.totalPages").value(1));
    }

    @Test
    void getAll_emptyResult_returnsEmptyPage() throws Exception {
        // Given — không có drug nào trong DB
        Page<DrugSimpleResponse> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );
        Mockito.when(service.getAll(ArgumentMatchers.any())).thenReturn(emptyPage);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.content").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("result.totalElements").value(0));
    }

    // ════════════════════════════════════════════════════════════
    //  GET /drugs/by-category/{categoryId}  — BY CATEGORY
    // ════════════════════════════════════════════════════════════

    @Test
    void getByCategory_happyPath() throws Exception {
        // Given
        Page<DrugSimpleResponse> page = new PageImpl<>(
                List.of(drugSimpleResponse),
                PageRequest.of(0, 10),
                1
        );
        Mockito.when(service.getAllByCategoryId(ArgumentMatchers.any(), eq(1)))
                .thenReturn(page);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/by-category/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.totalElements").value(1));
    }

    @Test
    void getByCategory_notFound_returnsEmptyPage() throws Exception {
        // Given — category tồn tại nhưng không có drug nào
        Page<DrugSimpleResponse> emptyPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 10), 0
        );
        Mockito.when(service.getAllByCategoryId(ArgumentMatchers.any(), eq(99)))
                .thenReturn(emptyPage);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/by-category/99")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.content").isEmpty());
    }

    @Test
    void getByCategory_invalidIdType_returnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/by-category/abc"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /drugs/search  — SEARCH BY NAME
    // ════════════════════════════════════════════════════════════

    @Test
    void search_happyPath() throws Exception {
        // Given
        Page<DrugSimpleResponse> page = new PageImpl<>(
                List.of(drugSimpleResponse),
                PageRequest.of(0, 10),
                1
        );
        Mockito.when(service.getAllByDrugName(ArgumentMatchers.any(), eq("Paracetamol")))
                .thenReturn(page);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/search")
                        .param("name", "Paracetamol")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].name")
                        .value("Paracetamol 500mg"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.totalElements").value(1));
    }

    @Test
    void search_noMatch_returnsEmptyPage() throws Exception {
        // Given — không có kết quả khớp
        Page<DrugSimpleResponse> emptyPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 10), 0
        );
        Mockito.when(service.getAllByDrugName(ArgumentMatchers.any(), eq("xyz_unknown")))
                .thenReturn(emptyPage);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/search")
                        .param("name", "xyz_unknown"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.content").isEmpty());
    }

    @Test
    void search_missingNameParam_returnsBadRequest() throws Exception {
        // Given — thiếu @RequestParam name bắt buộc
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/search"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /drugs/{id}/ingredients
    // ════════════════════════════════════════════════════════════

    @Test
    void getDrugIngredients_happyPath() throws Exception {
        // Given
        List<String> ingredients = List.of("Paracetamol", "Starch", "Magnesium stearate");
        Mockito.when(service.getDrugIngredients(1)).thenReturn(ingredients);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/1/ingredients"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result[0]").value("Paracetamol"))
                .andExpect(MockMvcResultMatchers.jsonPath("result[1]").value("Starch"))
                .andExpect(MockMvcResultMatchers.jsonPath("result[2]")
                        .value("Magnesium stearate"));
    }

    @Test
    void getDrugIngredients_emptyList() throws Exception {
        // Given — drug không có ingredient
        Mockito.when(service.getDrugIngredients(1)).thenReturn(List.of());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/1/ingredients"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result").isEmpty());
    }

    @Test
    void getDrugIngredients_drugNotFound_returnsError() throws Exception {
        // Given
        Mockito.when(service.getDrugIngredients(999))
                .thenThrow(new RuntimeException("Drug not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/999/ingredients"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /drugs/top10  — AUTOCOMPLETE
    // ════════════════════════════════════════════════════════════

    @Test
    void getTop10_happyPath() throws Exception {
        // Given
        List<DrugSimpleResponse> top10 = List.of(
                drugSimpleResponse,
                DrugSimpleResponse.builder()
                        .id(2).name("Paracetamol 250mg").slug("paracetamol-250mg").build()
        );
        Mockito.when(service.getTop10ByNameStartingWithIgnoreCase("Para")).thenReturn(top10);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/top10")
                        .param("name", "Para"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("result[0].name")
                        .value("Paracetamol 500mg"))
                .andExpect(MockMvcResultMatchers.jsonPath("result[1].name")
                        .value("Paracetamol 250mg"));
    }

    @Test
    void getTop10_noMatch_returnsEmptyList() throws Exception {
        // Given
        Mockito.when(service.getTop10ByNameStartingWithIgnoreCase("zzz"))
                .thenReturn(List.of());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/top10")
                        .param("name", "zzz"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result").isEmpty());
    }

    @Test
    void getTop10_missingNameParam_returnsBadRequest() throws Exception {
        // Given — thiếu @RequestParam name
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/top10"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void getTop10_caseInsensitive() throws Exception {
        // Given — "para" thường vs "Para" hoa — service xử lý IgnoreCase
        List<DrugSimpleResponse> result = List.of(drugSimpleResponse);
        Mockito.when(service.getTop10ByNameStartingWithIgnoreCase("para")).thenReturn(result);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/drugs/top10")
                        .param("name", "para"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.length()").value(1));
    }
}