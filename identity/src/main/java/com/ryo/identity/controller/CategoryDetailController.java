package com.ryo.identity.controller;

import com.ryo.identity.dto.request.CategoryDetailRequest;
import com.ryo.identity.dto.response.ApiResponse;
import com.ryo.identity.dto.response.CategoryDetailResponse;
import com.ryo.identity.dto.response.CategorySimpleResponse;
import com.ryo.identity.entity.CategoryDetail;
import com.ryo.identity.service.ICategoryDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category-detail")
@RequiredArgsConstructor
public class CategoryDetailController {

    private final ICategoryDetailService service;

    // --------------------------------------------------------
    // CREATE
    // --------------------------------------------------------
    @PostMapping
    public ApiResponse<CategoryDetailResponse> create(@RequestBody CategoryDetailRequest req) {
        return ApiResponse.<CategoryDetailResponse>builder()
                .result(service.create(req))
                .message("Created successfully")
                .build();
    }

    // --------------------------------------------------------
    // UPDATE
    // --------------------------------------------------------
    @PutMapping("/{id}")
    public ApiResponse<CategoryDetailResponse> update(
            @PathVariable Integer id,
            @RequestBody CategoryDetailRequest req
    ) {
        return ApiResponse.<CategoryDetailResponse>builder()
                .result(service.update(id, req))
                .message("Updated successfully")
                .build();
    }

    // --------------------------------------------------------
    // DELETE
    // --------------------------------------------------------
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }

    // --------------------------------------------------------
    // GET BY ID (detail object)
    // --------------------------------------------------------
    @GetMapping("/{id}")
    public ApiResponse<CategoryDetail> getOne(@PathVariable Integer id) {
        return ApiResponse.<CategoryDetail>builder()
                .result(service.get(id))
                .build();
    }

    // --------------------------------------------------------
    // LIST BY CATEGORY ID → trả về SimpleResponse
    // --------------------------------------------------------
    @GetMapping("/by-category/{categoryId}")
    public ApiResponse<Page<CategorySimpleResponse>> getByCategory(
            Pageable pageable,
            @PathVariable Integer categoryId
    ) {
        return ApiResponse.<Page<CategorySimpleResponse>>builder()
                .result(service.getCategoryDetailByCategoryId(pageable, categoryId))
                .build();
    }

    // --------------------------------------------------------
    // SEARCH BY NAME
    // --------------------------------------------------------
    @GetMapping("/search")
    public ApiResponse<Page<CategorySimpleResponse>> searchByName(
            Pageable pageable,
            @RequestParam String name
    ) {
        return ApiResponse.<Page<CategorySimpleResponse>>builder()
                .result(service.getCategoryDetailByCategoryDetailName(pageable, name))
                .build();
    }

    // --------------------------------------------------------
    // GET ALL (pagination)
    // --------------------------------------------------------
    @GetMapping
    public ApiResponse<Page<CategoryDetailResponse>> getAll(Pageable pageable) {
        return ApiResponse.<Page<CategoryDetailResponse>>builder()
                .result(service.getAll(pageable))
                .build();
    }
}
