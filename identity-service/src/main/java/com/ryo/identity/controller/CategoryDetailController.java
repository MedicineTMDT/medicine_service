package com.ryo.identity.controller;

import com.ryo.identity.dto.request.CategoryDetailRequest;
import com.ryo.identity.dto.response.APIResponse;
import com.ryo.identity.dto.response.CategoryDetailResponse;
import com.ryo.identity.dto.response.CategorySimpleResponse;
import com.ryo.identity.entity.CategoryDetail;
import com.ryo.identity.service.ICategoryDetailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/category-detail")
@RequiredArgsConstructor
@Tag(
        name = "CategoryDetail API",
        description = "API get category detail theo id, pageable, pageable + name, category id, thêm xóa sửa"
)
public class CategoryDetailController {

    private final ICategoryDetailService service;

    // --------------------------------------------------------
    // CREATE
    // --------------------------------------------------------
    @PostMapping
    public APIResponse<CategoryDetailResponse> create(@RequestBody CategoryDetailRequest req) {
        return APIResponse.<CategoryDetailResponse>builder()
                .result(service.create(req))
                .message("Created successfully")
                .build();
    }

    // --------------------------------------------------------
    // UPDATE
    // --------------------------------------------------------
    @PutMapping("/{id}")
    public APIResponse<CategoryDetailResponse> update(
            @PathVariable Integer id,
            @RequestBody CategoryDetailRequest req
    ) {
        return APIResponse.<CategoryDetailResponse>builder()
                .result(service.update(id, req))
                .message("Updated successfully")
                .build();
    }

    // --------------------------------------------------------
    // DELETE
    // --------------------------------------------------------
    @DeleteMapping("/{id}")
    public APIResponse<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return APIResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }

    // --------------------------------------------------------
    // GET BY ID (detail object)
    // --------------------------------------------------------
    @GetMapping("/{id}")
    public APIResponse<CategoryDetail> getOne(@PathVariable Integer id) {
        return APIResponse.<CategoryDetail>builder()
                .result(service.get(id))
                .build();
    }

    // --------------------------------------------------------
    // LIST BY CATEGORY ID → trả về SimpleResponse
    // --------------------------------------------------------
    @GetMapping("/by-category/{categoryId}")
    public APIResponse<Page<CategorySimpleResponse>> getByCategory(
            Pageable pageable,
            @PathVariable Integer categoryId
    ) {
        return APIResponse.<Page<CategorySimpleResponse>>builder()
                .result(service.getCategoryDetailByCategoryId(pageable, categoryId))
                .build();
    }

    // --------------------------------------------------------
    // SEARCH BY NAME
    // --------------------------------------------------------
    @GetMapping("/search")
    public APIResponse<Page<CategorySimpleResponse>> searchByName(
            Pageable pageable,
            @RequestParam String name
    ) {
        return APIResponse.<Page<CategorySimpleResponse>>builder()
                .result(service.getCategoryDetailByCategoryDetailName(pageable, name))
                .build();
    }

    // --------------------------------------------------------
    // GET ALL (pagination)
    // --------------------------------------------------------
    @GetMapping
    public APIResponse<Page<CategoryDetailResponse>> getAll(Pageable pageable) {
        return APIResponse.<Page<CategoryDetailResponse>>builder()
                .result(service.getAll(pageable))
                .build();
    }
}
