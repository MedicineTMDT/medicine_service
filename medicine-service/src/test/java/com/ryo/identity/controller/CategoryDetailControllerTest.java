package com.ryo.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryo.identity.dto.request.CategoryDetailRequest;
import com.ryo.identity.dto.response.CategoryDetailResponse;
import com.ryo.identity.dto.response.CategorySimpleResponse;
import com.ryo.identity.entity.CategoryDetail;
import com.ryo.identity.service.ICategoryDetailService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(CategoryDetailController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties")
public class CategoryDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICategoryDetailService service;

    private ObjectMapper objectMapper;

    private CategoryDetailRequest request;
    private CategoryDetailResponse response;
    private CategorySimpleResponse simpleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        request = CategoryDetailRequest.builder()
                .name("para")
                .content("sth")
                .categoryId(1)
                .build();

        response = CategoryDetailResponse.builder()
                .id(1)
                .name("Paracetamol")
                .build();

        simpleResponse = CategorySimpleResponse.builder()
                .id(1)
                .name("Paracetamol")
                .build();
    }

    // ========================================================
    // CREATE
    // ========================================================
    @Test
    void create() throws Exception {
        String content = objectMapper.writeValueAsString(request);

        Mockito.when(service.create(ArgumentMatchers.any()))
                .thenReturn(response);

        mockMvc.perform(post("/category-detail")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(1000))
                .andExpect(jsonPath("message").value("Created successfully"))
                .andExpect(jsonPath("result.name").value("Paracetamol"));
    }

    // ========================================================
    // UPDATE
    // ========================================================
    @Test
    void update() throws Exception {
        String content = objectMapper.writeValueAsString(request);

        Mockito.when(service.update(ArgumentMatchers.eq(1), ArgumentMatchers.any()))
                .thenReturn(response);

        mockMvc.perform(put("/category-detail/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("Updated successfully"))
                .andExpect(jsonPath("result.name").value("Paracetamol"));
    }

    // ========================================================
    // DELETE
    // ========================================================
    @Test
    void deleteCate() throws Exception {
        Mockito.doNothing().when(service).delete(1);

        mockMvc.perform(delete("/category-detail/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("Deleted successfully"));
    }

    // ========================================================
    // GET ONE
    // ========================================================
    @Test
    void getOne() throws Exception {
        CategoryDetail entity = new CategoryDetail();
        entity.setId(1);

        Mockito.when(service.get(1)).thenReturn(entity);

        mockMvc.perform(get("/category-detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(1000))
                .andExpect(jsonPath("result.id").value(1));
    }

    // ========================================================
    // GET BY CATEGORY ID
    // ========================================================
    @Test
    void getByCategory() throws Exception {
        PageImpl<CategorySimpleResponse> page =
                new PageImpl<>(List.of(simpleResponse));

        Mockito.when(service.getCategoryDetailByCategoryId(
                        ArgumentMatchers.any(), ArgumentMatchers.eq(1)))
                .thenReturn(page);

        mockMvc.perform(get("/category-detail/by-category/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result.content[0].name").value("Paracetamol"));
    }

    // ========================================================
    // SEARCH BY NAME
    // ========================================================
    @Test
    void searchByName() throws Exception {
        PageImpl<CategorySimpleResponse> page =
                new PageImpl<>(List.of(simpleResponse));

        Mockito.when(service.getCategoryDetailByCategoryDetailName(
                        ArgumentMatchers.any(), ArgumentMatchers.eq("para")))
                .thenReturn(page);

        mockMvc.perform(get("/category-detail/search")
                        .param("name", "para")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result.content[0].name").value("Paracetamol"));
    }

    // ========================================================
    // GET ALL
    // ========================================================
    @Test
    void getAll() throws Exception {
        PageImpl<CategoryDetailResponse> page =
                new PageImpl<>(List.of(response));

        Mockito.when(service.getAll(ArgumentMatchers.any()))
                .thenReturn(page);

        mockMvc.perform(get("/category-detail")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result.content[0].name").value("Paracetamol"));
    }
}