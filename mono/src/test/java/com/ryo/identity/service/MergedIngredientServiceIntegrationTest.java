package com.ryo.identity.service;


import com.ryo.identity.dto.response.MergedIngredientResponse;
import com.ryo.identity.entity.MergedIngredient;
import com.ryo.identity.repository.MergedIngredientRepository;
import com.ryo.identity.service.impl.MergedIngredientServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MergedIngredientServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private MergedIngredientServiceImpl mergedIngredientService;

    @Autowired
    private MergedIngredientRepository mergedIngredientRepository;

    @Test
    void suggest_shouldReturnRealData() {

        // given
        mergedIngredientRepository.save(
                MergedIngredient.builder()
                        .name("Paracetamol")
                        .build()
        );

        mergedIngredientRepository.save(
                MergedIngredient.builder()
                        .name("Panadol")
                        .build()
        );

        mergedIngredientRepository.save(
                MergedIngredient.builder()
                        .name("Vitamin C")
                        .build()
        );

        // when
        List<MergedIngredientResponse> result =
                mergedIngredientService.suggest("Pa");

        // then
        assertEquals(2, result.size());

        assertEquals("Paracetamol", result.get(0).getName());
    }
}
