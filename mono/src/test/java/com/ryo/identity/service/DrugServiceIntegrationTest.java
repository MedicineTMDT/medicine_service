package com.ryo.identity.service;

import com.ryo.identity.dto.request.DrugRequest;
import com.ryo.identity.dto.response.DrugResponse;
import com.ryo.identity.dto.response.DrugSimpleResponse;
import com.ryo.identity.entity.Drug;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.repository.DrugRepository;
import com.ryo.identity.service.impl.DrugServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DrugServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private DrugServiceImpl drugService;

    @Autowired
    private DrugRepository drugRepository;

    @Test
    void create_shouldPersistDrug() {
        authenticateAs("admin", "ADMIN");

        DrugResponse result = drugService.create(DrugRequest.builder()
                .name("Paracetamol 500")
                .slug("paracetamol-500")
                .content("Pain relief")
                .document("Doc")
                .image(List.of("https://cdn.example.com/para.jpg"))
                .ingredient(List.of("Paracetamol"))
                .metadata(Map.of("brand", "Ryo"))
                .info(Map.of("dose", "500mg"))
                .build());

        assertNotNull(result.getId());
        assertEquals("Paracetamol 500", result.getName());
        assertTrue(drugRepository.findBySlug("paracetamol-500").isPresent());
    }

    @Test
    void create_whenSlugExists_shouldThrowAppException() {
        authenticateAs("admin", "ADMIN");
        drugRepository.save(Drug.builder()
                .name("Existing")
                .slug("existing")
                .build());

        assertThrows(AppException.class, () -> drugService.create(DrugRequest.builder()
                .name("Other")
                .slug("existing")
                .build()));
    }

    @Test
    void update_shouldChangeDrugFields() {
        authenticateAs("admin", "ADMIN");
        Drug drug = drugRepository.save(Drug.builder()
                .name("Old")
                .slug("old")
                .ingredient(List.of("Old ingredient"))
                .build());

        DrugResponse result = drugService.update(DrugRequest.builder()
                .name("New")
                .content("New content")
                .document("New document")
                .image(List.of("image.jpg"))
                .ingredient(List.of("New ingredient"))
                .metadata(Map.of("key", "value"))
                .info(Map.of("warning", "none"))
                .build(), drug.getId());

        assertEquals("New", result.getName());
        assertEquals(List.of("New ingredient"), drugRepository.findById(drug.getId()).orElseThrow().getIngredient());
    }

    @Test
    void getAllByDrugName_shouldReturnProjectedMatches() {
        drugRepository.save(Drug.builder()
                .name("Panadol")
                .slug("panadol")
                .image(List.of("panadol.jpg"))
                .build());
        drugRepository.save(Drug.builder()
                .name("Vitamin C")
                .slug("vitamin-c")
                .build());

        Page<DrugSimpleResponse> result = drugService.getAllByDrugName(PageRequest.of(0, 10), "pan");

        assertEquals(1, result.getTotalElements());
        assertEquals("Panadol", result.getContent().getFirst().getName());
        assertEquals("panadol.jpg", result.getContent().getFirst().getImageLink());
    }

    @Test
    void getDrugIngredients_shouldReturnIngredientList() {
        Drug drug = drugRepository.save(Drug.builder()
                .name("Aspirin")
                .slug("aspirin")
                .ingredient(List.of("Acetylsalicylic acid"))
                .build());

        List<String> result = drugService.getDrugIngredients(drug.getId());

        assertEquals(List.of("Acetylsalicylic acid"), result);
    }

    @Test
    void getTop10ByNameStartingWithIgnoreCase_shouldReturnSuggestions() {
        drugRepository.save(Drug.builder().name("Paracetamol").slug("paracetamol").build());
        drugRepository.save(Drug.builder().name("Panadol").slug("panadol").build());
        drugRepository.save(Drug.builder().name("Vitamin C").slug("vitamin-c").build());

        List<DrugSimpleResponse> result = drugService.getTop10ByNameStartingWithIgnoreCase("pa");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Paracetamol")));
    }
}
