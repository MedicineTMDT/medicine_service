package com.ryo.identity.controller;

import com.ryo.identity.dto.request.CategoryRequest;
import com.ryo.identity.dto.response.ApiResponse;
import com.ryo.identity.dto.response.CategoryResponse;
import com.ryo.identity.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    // CREATE
    @PostMapping
    public ApiResponse<CategoryResponse> create(@RequestBody CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.create(request))
                .build();
    }

    // UPDATE
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(
            @PathVariable Integer id,
            @RequestBody CategoryRequest request
    ) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.update(id, request))
                .build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }

    // GET ONE
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> get(@PathVariable Integer id) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.get(id))
                .build();
    }

    // GET ALL + PAGINATION
    @GetMapping
    public ApiResponse<Page<CategoryResponse>> getAll(Pageable pageable) {
        return ApiResponse.<Page<CategoryResponse>>builder()
                .result(categoryService.getAll(pageable))
                .build();
    }

    // SEARCH BY NAME + PAGINATION
    @GetMapping("/search")
    public ApiResponse<Page<CategoryResponse>> search(
            @RequestParam String name,
            Pageable pageable
    ) {
        return ApiResponse.<Page<CategoryResponse>>builder()
                .result(categoryService.getByName(pageable, name))
                .build();
    }
}
