package com.ryo.identity.controller;

import com.ryo.identity.dto.request.DrugInteractionRequest;
import com.ryo.identity.dto.response.APIResponse;
import com.ryo.identity.entity.DrugInteraction;
import com.ryo.identity.service.IDrugInteractionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drug-interactions")
@RequiredArgsConstructor
@Tag(
        name = "Drug-Interaction API",
        description = "API tra cứu tương tác theo id, tra theo list ingredient id."
)
public class DrugInteractionController {

    private final IDrugInteractionService interactionService;

    // CREATE
    @PostMapping
    public APIResponse<DrugInteraction> create(@RequestBody DrugInteractionRequest request) {
        return APIResponse.<DrugInteraction>builder()
                .result(interactionService.create(request))
                .build();
    }

    // UPDATE
    @PutMapping("/{id}")
    public APIResponse<DrugInteraction> update(
            @PathVariable Integer id,
            @RequestBody DrugInteractionRequest request
    ) {
        return APIResponse.<DrugInteraction>builder()
                .result(interactionService.update(request, id))
                .build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public APIResponse<Void> delete(@PathVariable Integer id) {
        interactionService.delete(id);
        return APIResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }

    // GET ONE
    @GetMapping("/{id}")
    public APIResponse<DrugInteraction> get(@PathVariable Integer id) {
        return APIResponse.<DrugInteraction>builder()
                .result(interactionService.get(id))
                .build();
    }

    // GET BY LIST OF INGREDIENT NAMES
    @GetMapping("/search-by-ingredients")
    public APIResponse<List<DrugInteraction>> findByIngredientNames(
            @RequestParam List<String> ingredientNames
    ) {
        return APIResponse.<List<DrugInteraction>>builder()
                .result(interactionService.getByListIngredientName(ingredientNames))
                .build();
    }

    // GET by one ingredient name
//    @GetMapping("/find-by-ingredient")
//    public APIResponse<List<DrugInteraction>> findByOneIngredientName(
//            @RequestParam String name
//    ) {
//        return APIResponse.<List<DrugInteraction>>builder()
//                .result(interactionService.getByListIngredientName(List.of(name)))
//                .build();
//    }
}
