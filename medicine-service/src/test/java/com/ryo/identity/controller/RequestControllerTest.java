package com.ryo.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryo.identity.constant.TypeOfRequest;
import com.ryo.identity.dto.request.CreateSuggestionRequest;
import com.ryo.identity.entity.Request;
import com.ryo.identity.service.IRequestService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Slf4j
@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties")
public class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IRequestService requestService;

    private ObjectMapper objectMapper;

    private CreateSuggestionRequest createSuggestionRequest;
    private Request request;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        createSuggestionRequest = new CreateSuggestionRequest();
        createSuggestionRequest.setTitle("Thêm tính năng tìm kiếm nâng cao");
        createSuggestionRequest.setContent("Người dùng muốn lọc theo nhiều tiêu chí");
        createSuggestionRequest.setTypeOfRequest(TypeOfRequest.ADD);

        request = Request.builder()
                .id("request-001")
                .title("Thêm tính năng tìm kiếm nâng cao")
                .content("Người dùng muốn lọc theo nhiều tiêu chí")
                .typeOfRequest(TypeOfRequest.ADD)
                .proceed(false)
                .build();
    }

    // ════════════════════════════════════════════════════════════
    //  POST /requests  — CREATE
    // ════════════════════════════════════════════════════════════

    @Test
    void create_happyPath() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(createSuggestionRequest);
        Mockito.when(requestService.createRequest(ArgumentMatchers.any()))
                .thenReturn(request);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("request-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("title")
                        .value("Thêm tính năng tìm kiếm nâng cao"))
                .andExpect(MockMvcResultMatchers.jsonPath("typeOfRequest")
                        .value("ADD"))
                .andExpect(MockMvcResultMatchers.jsonPath("proceed").value(false));
    }

    @Test
    void create_allTypeOfRequest() throws Exception {
        // Given — lần lượt test 3 loại type
        for (TypeOfRequest type : TypeOfRequest.values()) {
            createSuggestionRequest.setTypeOfRequest(type);
            request = Request.builder()
                    .id("request-001")
                    .title("Title")
                    .content("Content")
                    .typeOfRequest(type)
                    .proceed(false)
                    .build();

            String content = objectMapper.writeValueAsString(createSuggestionRequest);
            Mockito.when(requestService.createRequest(ArgumentMatchers.any()))
                    .thenReturn(request);

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/requests")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(content))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("typeOfRequest")
                            .value(type.name()));
        }
    }

    @Test
    void create_emptyBody_stillCallsService() throws Exception {
        // Given — không có @Valid nên body rỗng vẫn qua controller
        Mockito.when(requestService.createRequest(ArgumentMatchers.any()))
                .thenReturn(new Request());

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void create_serviceThrowsException_returnsError() throws Exception {
        // Given
        String content = objectMapper.writeValueAsString(createSuggestionRequest);
        Mockito.when(requestService.createRequest(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("User not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /requests  — GET ALL
    // ════════════════════════════════════════════════════════════

    @Test
    void getAll_happyPath() throws Exception {
        // Given
        Page<Request> page = new PageImpl<>(
                List.of(request), PageRequest.of(0, 10), 1
        );
        Mockito.when(requestService.getAllRequest(ArgumentMatchers.any()))
                .thenReturn(page);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content[0].id")
                        .value("request-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("content[0].typeOfRequest")
                        .value("ADD"))
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("totalPages").value(1));
    }

    @Test
    void getAll_emptyResult_returnsEmptyPage() throws Exception {
        // Given — chưa có request nào
        Page<Request> emptyPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 10), 0
        );
        Mockito.when(requestService.getAllRequest(ArgumentMatchers.any()))
                .thenReturn(emptyPage);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(0));
    }

    @Test
    void getAll_multiplePages() throws Exception {
        // Given — 25 phần tử, page size 10 → 3 trang
        Page<Request> page = new PageImpl<>(
                List.of(request), PageRequest.of(0, 10), 25
        );
        Mockito.when(requestService.getAllRequest(ArgumentMatchers.any()))
                .thenReturn(page);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(25))
                .andExpect(MockMvcResultMatchers.jsonPath("totalPages").value(3));
    }

    // ════════════════════════════════════════════════════════════
    //  GET /requests/type  — GET BY TYPE
    // ════════════════════════════════════════════════════════════

    @Test
    void getByType_ADD_happyPath() throws Exception {
        // Given
        Page<Request> page = new PageImpl<>(
                List.of(request), PageRequest.of(0, 10), 1
        );
        Mockito.when(requestService.getAllRequestByTypeOfReques(
                        ArgumentMatchers.any(), eq(TypeOfRequest.ADD)))
                .thenReturn(page);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/type")
                        .param("type", "ADD")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content[0].typeOfRequest")
                        .value("ADD"))
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1));
    }

    @Test
    void getByType_EDIT_happyPath() throws Exception {
        // Given
        Request editRequest = Request.builder()
                .id("request-002")
                .title("Chỉnh sửa thông tin thuốc")
                .content("Cập nhật mô tả cho đúng hơn")
                .typeOfRequest(TypeOfRequest.EDIT)
                .proceed(false)
                .build();

        Page<Request> page = new PageImpl<>(
                List.of(editRequest), PageRequest.of(0, 10), 1
        );
        Mockito.when(requestService.getAllRequestByTypeOfReques(
                        ArgumentMatchers.any(), eq(TypeOfRequest.EDIT)))
                .thenReturn(page);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/type")
                        .param("type", "EDIT"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content[0].typeOfRequest")
                        .value("EDIT"));
    }

    @Test
    void getByType_QUESTION_happyPath() throws Exception {
        // Given
        Request questionRequest = Request.builder()
                .id("request-003")
                .title("Hỏi về tác dụng phụ")
                .content("Thuốc X có gây buồn ngủ không?")
                .typeOfRequest(TypeOfRequest.QUESTION)
                .proceed(false)
                .build();

        Page<Request> page = new PageImpl<>(
                List.of(questionRequest), PageRequest.of(0, 10), 1
        );
        Mockito.when(requestService.getAllRequestByTypeOfReques(
                        ArgumentMatchers.any(), eq(TypeOfRequest.QUESTION)))
                .thenReturn(page);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/type")
                        .param("type", "QUESTION"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content[0].typeOfRequest")
                        .value("QUESTION"));
    }

    @Test
    void getByType_noMatch_returnsEmptyPage() throws Exception {
        // Given — không có request nào với type này
        Page<Request> emptyPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 10), 0
        );
        Mockito.when(requestService.getAllRequestByTypeOfReques(
                        ArgumentMatchers.any(), eq(TypeOfRequest.ADD)))
                .thenReturn(emptyPage);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/type")
                        .param("type", "ADD"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(0));
    }

    @Test
    void getByType_invalidType_returnsBadRequest() throws Exception {
        // Given — type không nằm trong enum TypeOfRequest
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/type")
                        .param("type", "INVALID_TYPE"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void getByType_missingTypeParam_returnsBadRequest() throws Exception {
        // Given — thiếu @RequestParam type bắt buộc
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/type"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════
    //  GET /requests/user/{userId}  — GET BY USER
    // ════════════════════════════════════════════════════════════

    @Test
    void getByUser_happyPath() throws Exception {
        // Given
        Page<Request> page = new PageImpl<>(
                List.of(request), PageRequest.of(0, 10), 1
        );
        Mockito.when(requestService.getAllRequestByUserId(
                        ArgumentMatchers.any(), eq("user-001")))
                .thenReturn(page);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/user/user-001")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content[0].id")
                        .value("request-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("content[0].title")
                        .value("Thêm tính năng tìm kiếm nâng cao"))
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1));
    }

    @Test
    void getByUser_noRequest_returnsEmptyPage() throws Exception {
        // Given — user tồn tại nhưng chưa gửi request nào
        Page<Request> emptyPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 10), 0
        );
        Mockito.when(requestService.getAllRequestByUserId(
                        ArgumentMatchers.any(), eq("user-002")))
                .thenReturn(emptyPage);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/user/user-002"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(0));
    }

    @Test
    void getByUser_multipleRequests() throws Exception {
        // Given — user gửi nhiều loại request khác nhau
        Request r2 = Request.builder()
                .id("request-002")
                .title("Báo lỗi hiển thị")
                .content("Màn hình bị lỗi font")
                .typeOfRequest(TypeOfRequest.EDIT)
                .proceed(true)
                .build();

        Page<Request> page = new PageImpl<>(
                List.of(request, r2), PageRequest.of(0, 10), 2
        );
        Mockito.when(requestService.getAllRequestByUserId(
                        ArgumentMatchers.any(), eq("user-001")))
                .thenReturn(page);

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/user/user-001"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("content[0].typeOfRequest")
                        .value("ADD"))
                .andExpect(MockMvcResultMatchers.jsonPath("content[1].typeOfRequest")
                        .value("EDIT"))
                .andExpect(MockMvcResultMatchers.jsonPath("content[1].proceed")
                        .value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(2));
    }

    @Test
    void getByUser_userNotFound_returnsError() throws Exception {
        // Given — service ném exception khi userId không tồn tại
        Mockito.when(requestService.getAllRequestByUserId(
                        ArgumentMatchers.any(), eq("not-exist")))
                .thenThrow(new RuntimeException("User not found"));

        // When Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/user/not-exist"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}