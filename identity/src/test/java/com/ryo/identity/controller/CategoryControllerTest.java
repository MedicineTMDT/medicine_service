package com.ryo.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryo.identity.dto.request.CategoryRequest;
import com.ryo.identity.dto.response.CategoryResponse;
import com.ryo.identity.service.ICategoryService;
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
import org.springframework.data.domain.PageImpl;
import java.util.List;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Slf4j
@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties")
public class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICategoryService categoryService;

    private ObjectMapper objectMapper;

    // ── Request objects ──────────────────────────────────────────
    private CategoryRequest categoryRequest;

    // ── Response objects ─────────────────────────────────────────
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        categoryRequest = CategoryRequest.builder()
                .name("Paracetamol")
                .slug("para")
                .build();

        categoryResponse = CategoryResponse.builder()
                .name("Paracetamol")
                .build();
    }

    @Test
    void create() throws Exception {
        String content = objectMapper.writeValueAsString(categoryRequest);
        Mockito.when(categoryService.create(ArgumentMatchers.any()))
                .thenReturn(categoryResponse);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name").value("Paracetamol"));
    }

    @Test
    void update() throws Exception {
        String content = objectMapper.writeValueAsString(categoryRequest);

        Mockito.when(categoryService.update(ArgumentMatchers.eq(1), ArgumentMatchers.any()))
                .thenReturn(categoryResponse);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name").value("Paracetamol"));
    }

    @Test
    void delete() throws Exception {
        Mockito.doNothing().when(categoryService).delete(1);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/categories/1")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Deleted successfully"));
    }

    @Test
    void get() throws Exception {
        Mockito.when(categoryService.get(1))
                .thenReturn(categoryResponse);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/categories/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name").value("Paracetamol"));
    }

    @Test
    void getAll() throws Exception {
        PageImpl<CategoryResponse> page =
                new PageImpl<>(List.of(categoryResponse));

        Mockito.when(categoryService.getAll(ArgumentMatchers.any()))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/categories")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].name")
                        .value("Paracetamol"));
    }

    @Test
    void search() throws Exception {
        PageImpl<CategoryResponse> page =
                new PageImpl<>(List.of(categoryResponse));

        Mockito.when(categoryService.getByName(ArgumentMatchers.any(), ArgumentMatchers.eq("para")))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/categories/search")
                        .param("name", "para")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].name")
                        .value("Paracetamol"));
    }

    @Test
    void create_serviceThrowsException_returns400() throws Exception {
        String content = objectMapper.writeValueAsString(categoryRequest);

        Mockito.when(categoryService.create(ArgumentMatchers.any()))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
