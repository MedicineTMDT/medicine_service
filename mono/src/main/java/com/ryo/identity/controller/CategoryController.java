package com.ryo.identity.controller;

import com.ryo.identity.dto.request.CategoryRequest;
import com.ryo.identity.dto.response.APIResponse;
import com.ryo.identity.dto.response.CategoryResponse;
import com.ryo.identity.service.ICategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(
        name = "Category API",
        description = "API tạo, cập nhật, xóa, tìm kiếm chi tiết, tìm kiếm theo phân trang, tìm kiếm theo tên."
)
public class CategoryController {

    private final ICategoryService categoryService;

    // CREATE
    @PostMapping
    public APIResponse<CategoryResponse> create(@RequestBody CategoryRequest request) {
        return APIResponse.<CategoryResponse>builder()
                .result(categoryService.create(request))
                .build();
    }

    // UPDATE
    @PutMapping("/{id}")
    public APIResponse<CategoryResponse> update(
            @PathVariable Integer id,
            @RequestBody CategoryRequest request
    ) {
        return APIResponse.<CategoryResponse>builder()
                .result(categoryService.update(id, request))
                .build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public APIResponse<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return APIResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }

    // GET ONE
    @GetMapping("/{id}")
    public APIResponse<CategoryResponse> get(@PathVariable Integer id) {
        return APIResponse.<CategoryResponse>builder()
                .result(categoryService.get(id))
                .build();
    }

    // GET ALL + PAGINATION
    @GetMapping
    public APIResponse<Page<CategoryResponse>> getAll(Pageable pageable) {
        return APIResponse.<Page<CategoryResponse>>builder()
                .result(categoryService.getAll(pageable))
                .build();
    }

    // SEARCH BY NAME + PAGINATION
    @GetMapping("/search")
    public APIResponse<Page<CategoryResponse>> search(
            @RequestParam String name,
            Pageable pageable
    ) {
        return APIResponse.<Page<CategoryResponse>>builder()
                .result(categoryService.getByName(pageable, name))
                .build();
    }
}
