package com.ryo.identity.controller;

import com.ryo.identity.dto.request.DrugRequest;
import com.ryo.identity.dto.response.APIResponse;
import com.ryo.identity.dto.response.DrugResponse;
import com.ryo.identity.dto.response.DrugSimpleResponse;
import com.ryo.identity.entity.Drug;
import com.ryo.identity.service.IDrugService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drugs")
@RequiredArgsConstructor
@Tag(
        name = "Drug API",
        description = "API tạo, cập nhật, xóa, tìm kiếm chi tiết, tìm kiếm theo phân trang, tìm kiếm theo tên."
)
public class DrugController {

    private final IDrugService drugService;

    // CREATE
    @PostMapping
    public APIResponse<DrugResponse> create(@RequestBody DrugRequest request) {
        return APIResponse.<DrugResponse>builder()
                .result(drugService.create(request))
                .build();
    }

    // UPDATE
    @PutMapping("/{id}")
    public APIResponse<DrugResponse> update(
            @PathVariable Integer id,
            @RequestBody DrugRequest request
    ) {
        return APIResponse.<DrugResponse>builder()
                .result(drugService.update(request, id))
                .build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public APIResponse<Void> delete(@PathVariable Integer id) {
        drugService.delete(id);
        return APIResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }

    // GET
    @GetMapping("/{id}")
    public APIResponse<Drug> get(@PathVariable Integer id) {
        return APIResponse.<Drug>builder()
                .result(drugService.get(id))
                .build();
    }

    // GET ALL - PAGINATION
    @GetMapping
    public APIResponse<Page<DrugSimpleResponse>> getAll(Pageable pageable) {
        return APIResponse.<Page<DrugSimpleResponse>>builder()
                .result(drugService.getAll(pageable))
                .build();
    }

    // GET BY CATEGORY
    @GetMapping("/by-category/{categoryId}")
    public APIResponse<Page<DrugSimpleResponse>> getByCategory(
            Pageable pageable,
            @PathVariable Integer categoryId
    ) {
        return APIResponse.<Page<DrugSimpleResponse>>builder()
                .result(drugService.getAllByCategoryId(pageable, categoryId))
                .build();
    }

    // SEARCH BY DRUG NAME
    @GetMapping("/search")
    public APIResponse<Page<DrugSimpleResponse>> getByDrugName(
            Pageable pageable,
            @RequestParam String name
    ) {
        return APIResponse.<Page<DrugSimpleResponse>>builder()
                .result(drugService.getAllByDrugName(pageable, name))
                .build();
    }

    // GET INGREDIENTS BY DRUG ID
    @GetMapping("/{id}/ingredients")
    public APIResponse<List<String>> getDrugIngredients(@PathVariable Integer id) {
        return APIResponse.<List<String>>builder()
                .result(drugService.getDrugIngredients(id))
                .build();
    }

    // AUTOCOMPLETE (TOP 10)
    @GetMapping("/top10")
    public APIResponse<List<DrugSimpleResponse>> getTop10(@RequestParam String name) {
        return APIResponse.<List<DrugSimpleResponse>>builder()
                .result(drugService.getTop10ByNameStartingWithIgnoreCase(name))
                .build();
    }
}
