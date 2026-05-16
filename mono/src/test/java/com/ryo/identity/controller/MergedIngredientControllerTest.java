package com.ryo.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryo.identity.dto.response.MergedIngredientResponse;
import com.ryo.identity.service.IMergedIngredientService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@Slf4j
@WebMvcTest(MergedIngredientController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties")
public class MergedIngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IMergedIngredientService mergedIngredientService;

    private ObjectMapper objectMapper;

    private MergedIngredientResponse ingredientResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ingredientResponse = MergedIngredientResponse.builder().build();
        ingredientResponse.setId(1);
        ingredientResponse.setName("Paracetamol");
    }

    // ════════════════════════════════════════════════════════════
    //  GET /api/v1/merged-ingredient/suggest
    // ════════════════════════════════════════════════════════════

    @Test
    void suggestIngredients_happyPath() throws Exception {
        // Given
        Mockito.when(mergedIngredientService.suggest("Para"))
                .thenReturn(List.of(ingredientResponse));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/merged-ingredient/suggest")
                        .param("name", "Para"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name")
                        .value("Paracetamol"));
    }

    @Test
    void suggestIngredients_multipleResults() throws Exception {
        // Given
        MergedIngredientResponse second = MergedIngredientResponse.builder().build();
        second.setId(2);
        second.setName("Paroxetine");

        Mockito.when(mergedIngredientService.suggest("Par"))
                .thenReturn(List.of(ingredientResponse, second));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/merged-ingredient/suggest")
                        .param("name", "Par"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()")
                        .value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name")
                        .value("Paroxetine"));
    }

    @Test
    void suggestIngredients_noMatch_returnsEmptyList() throws Exception {
        // Given
        Mockito.when(mergedIngredientService.suggest("xyz"))
                .thenReturn(List.of());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/merged-ingredient/suggest")
                        .param("name", "xyz"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    void suggestIngredients_missingParam_returnsBadRequest() throws Exception {
        // Given When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/merged-ingredient/suggest"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void suggestIngredients_emptyParam_returnsEmptyList() throws Exception {
        // Given
        Mockito.when(mergedIngredientService.suggest(""))
                .thenReturn(List.of());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/merged-ingredient/suggest")
                        .param("name", ""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    void suggestIngredients_serviceThrowsException_returnsError() throws Exception {
        // Given
        Mockito.when(mergedIngredientService.suggest(ArgumentMatchers.anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/merged-ingredient/suggest")
                        .param("name", "Para"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}