package com.ryo.identity.controller;

import com.ryo.identity.dto.request.DrugRequest;
import com.ryo.identity.dto.response.ApiResponse;
import com.ryo.identity.dto.response.DrugResponse;
import com.ryo.identity.dto.response.DrugSimpleResponse;
import com.ryo.identity.entity.Drug;
import com.ryo.identity.service.IDrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drugs")
@RequiredArgsConstructor
public class DrugController {

    private final IDrugService drugService;

    // CREATE
    @PostMapping
    public ApiResponse<DrugResponse> create(@RequestBody DrugRequest request) {
        return ApiResponse.<DrugResponse>builder()
                .result(drugService.create(request))
                .build();
    }

    // UPDATE
    @PutMapping("/{id}")
    public ApiResponse<DrugResponse> update(
            @PathVariable Integer id,
            @RequestBody DrugRequest request
    ) {
        return ApiResponse.<DrugResponse>builder()
                .result(drugService.update(request, id))
                .build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        drugService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }

    // GET
    @GetMapping("/{id}")
    public ApiResponse<Drug> get(@PathVariable Integer id) {
        return ApiResponse.<Drug>builder()
                .result(drugService.get(id))
                .build();
    }

    // GET ALL - PAGINATION
    @GetMapping
    public ApiResponse<Page<DrugSimpleResponse>> getAll(Pageable pageable) {
        return ApiResponse.<Page<DrugSimpleResponse>>builder()
                .result(drugService.getAll(pageable))
                .build();
    }

    // GET BY CATEGORY
    @GetMapping("/by-category/{categoryId}")
    public ApiResponse<Page<DrugSimpleResponse>> getByCategory(
            Pageable pageable,
            @PathVariable Integer categoryId
    ) {
        return ApiResponse.<Page<DrugSimpleResponse>>builder()
                .result(drugService.getAllByCategoryId(pageable, categoryId))
                .build();
    }

    // SEARCH BY DRUG NAME
    @GetMapping("/search")
    public ApiResponse<Page<DrugSimpleResponse>> getByDrugName(
            Pageable pageable,
            @RequestParam String name
    ) {
        return ApiResponse.<Page<DrugSimpleResponse>>builder()
                .result(drugService.getAllByDrugName(pageable, name))
                .build();
    }

    // GET INGREDIENTS BY DRUG ID
    @GetMapping("/{id}/ingredients")
    public ApiResponse<List<String>> getDrugIngredients(@PathVariable Integer id) {
        return ApiResponse.<List<String>>builder()
                .result(drugService.getDrugIngredients(id))
                .build();
    }

    // AUTOCOMPLETE (TOP 10)
    @GetMapping("/top10")
    public ApiResponse<List<DrugSimpleResponse>> getTop10(@RequestParam String name) {
        return ApiResponse.<List<DrugSimpleResponse>>builder()
                .result(drugService.getTop10ByNameStartingWithIgnoreCase(name))
                .build();
    }
}
