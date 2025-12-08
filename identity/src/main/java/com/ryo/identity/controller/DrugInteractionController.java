package com.ryo.identity.controller;

import com.ryo.identity.dto.request.DrugInteractionRequest;
import com.ryo.identity.dto.response.ApiResponse;
import com.ryo.identity.entity.DrugInteraction;
import com.ryo.identity.service.IDrugInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drug-interactions")
@RequiredArgsConstructor
public class DrugInteractionController {

    private final IDrugInteractionService interactionService;

    // CREATE
    @PostMapping
    public ApiResponse<DrugInteraction> create(@RequestBody DrugInteractionRequest request) {
        return ApiResponse.<DrugInteraction>builder()
                .result(interactionService.create(request))
                .build();
    }

    // UPDATE
    @PutMapping("/{id}")
    public ApiResponse<DrugInteraction> update(
            @PathVariable Integer id,
            @RequestBody DrugInteractionRequest request
    ) {
        return ApiResponse.<DrugInteraction>builder()
                .result(interactionService.update(request, id))
                .build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        interactionService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }

    // GET ONE
    @GetMapping("/{id}")
    public ApiResponse<DrugInteraction> get(@PathVariable Integer id) {
        return ApiResponse.<DrugInteraction>builder()
                .result(interactionService.get(id))
                .build();
    }

    // GET BY LIST OF INGREDIENT NAMES
    @PostMapping("/search-by-ingredients")
    public ApiResponse<List<DrugInteraction>> findByIngredientNames(
            @RequestBody List<String> ingredientNames
    ) {
        return ApiResponse.<List<DrugInteraction>>builder()
                .result(interactionService.getByListIngredientName(ingredientNames))
                .build();
    }

    // GET by one ingredient name
    @GetMapping("/find-by-ingredient")
    public ApiResponse<List<DrugInteraction>> findByOneIngredientName(
            @RequestParam String name
    ) {
        return ApiResponse.<List<DrugInteraction>>builder()
                .result(interactionService.getByListIngredientName(List.of(name)))
                .build();
    }
}
