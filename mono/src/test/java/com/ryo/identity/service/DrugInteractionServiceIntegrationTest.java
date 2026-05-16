package com.ryo.identity.service;

import com.ryo.identity.dto.request.DrugInteractionRequest;
import com.ryo.identity.entity.DrugInteraction;
import com.ryo.identity.entity.MergedIngredient;
import com.ryo.identity.repository.DrugInteractionRepository;
import com.ryo.identity.repository.MergedIngredientRepository;
import com.ryo.identity.service.impl.DrugInteractionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DrugInteractionServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private DrugInteractionServiceImpl drugInteractionService;

    @Autowired
    private DrugInteractionRepository drugInteractionRepository;

    @Autowired
    private MergedIngredientRepository mergedIngredientRepository;

    @Test
    void create_shouldPersistInteractionWithIngredients() {
        authenticateAs("admin", "ADMIN");
        MergedIngredient paracetamol = saveIngredient("Paracetamol");
        MergedIngredient warfarin = saveIngredient("Warfarin");

        DrugInteraction result = drugInteractionService.create(request(
                "High", "Bleeding risk", "Mechanism", "Monitor",
                "Paracetamol", "Warfarin", paracetamol.getId(), warfarin.getId()));

        assertNotNull(result.getId());
        assertEquals("High", result.getMucDoNghiemTrong());
        assertEquals(paracetamol.getId(), result.getIngredient1().getId());
        assertEquals(warfarin.getId(), result.getIngredient2().getId());
    }

    @Test
    void update_shouldChangeInteractionFields() {
        authenticateAs("admin", "ADMIN");
        DrugInteraction interaction = drugInteractionRepository.save(DrugInteraction.builder()
                .mucDoNghiemTrong("Low")
                .hoatChat1Name("A")
                .hoatChat2Name("B")
                .build());

        DrugInteraction result = drugInteractionService.update(request(
                "Medium", "Updated consequence", "Updated mechanism", "Updated action",
                "C", "D", null, null), interaction.getId());

        assertEquals("Medium", result.getMucDoNghiemTrong());
        assertEquals("C", result.getHoatChat1Name());
        assertNull(result.getIngredient1());
    }

    @Test
    void getByListIngredientName_withTwoNames_shouldFindInteractionBetweenThem() {
        drugInteractionRepository.save(DrugInteraction.builder()
                .hoatChat1Name("Paracetamol")
                .hoatChat2Name("Warfarin")
                .mucDoNghiemTrong("High")
                .build());

        List<DrugInteraction> result =
                drugInteractionService.getByListIngredientName(List.of("Warfarin", "Paracetamol"));

        assertEquals(1, result.size());
        assertEquals("High", result.getFirst().getMucDoNghiemTrong());
    }

    @Test
    void getByListIngredientName_withOneName_shouldFindAnyInteraction() {
        drugInteractionRepository.save(DrugInteraction.builder()
                .hoatChat1Name("Aspirin")
                .hoatChat2Name("Ibuprofen")
                .build());

        List<DrugInteraction> result =
                drugInteractionService.getByListIngredientName(List.of("Aspirin"));

        assertEquals(1, result.size());
    }

    @Test
    void getByListIngredientName_whenNull_shouldReturnEmptyList() {
        assertTrue(drugInteractionService.getByListIngredientName(null).isEmpty());
    }

    @Test
    void getAll_shouldReturnSavedInteractions() {
        drugInteractionRepository.save(DrugInteraction.builder()
                .hoatChat1Name("A")
                .hoatChat2Name("B")
                .build());

        Page<DrugInteraction> result = drugInteractionService.getAll(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    private MergedIngredient saveIngredient(String name) {
        return mergedIngredientRepository.save(MergedIngredient.builder().name(name).build());
    }

    private DrugInteractionRequest request(
            String severity,
            String consequence,
            String mechanism,
            String action,
            String ingredient1Name,
            String ingredient2Name,
            Integer ingredient1Id,
            Integer ingredient2Id
    ) {
        DrugInteractionRequest request = new DrugInteractionRequest();
        request.setMucDoNghiemTrong(severity);
        request.setHauQuaCuaTuongTac(consequence);
        request.setCoCheTuongTac(mechanism);
        request.setXuTriTuongTac(action);
        request.setHoatChat1Name(ingredient1Name);
        request.setHoatChat2Name(ingredient2Name);
        request.setIngredient1Id(ingredient1Id);
        request.setIngredient2Id(ingredient2Id);
        return request;
    }
}
