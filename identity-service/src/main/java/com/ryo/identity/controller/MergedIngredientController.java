package com.ryo.identity.controller;

import com.ryo.identity.dto.response.MergedIngredientResponse;
import com.ryo.identity.service.IMergedIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merged-ingredient")
@RequiredArgsConstructor
public class MergedIngredientController {

    private final IMergedIngredientService mergedIngredientService;

    @GetMapping("/suggest")
    public List<MergedIngredientResponse> suggestIngredients(
            @RequestParam String name
    ) {
        return mergedIngredientService.suggest(name);
    }
}
