package com.ryo.identity.service.impl;

import com.ryo.identity.dto.request.DrugInteractionRequest;
import com.ryo.identity.entity.DrugInteraction;
import com.ryo.identity.entity.MergedIngredient;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.exception.ErrorCode;
import com.ryo.identity.repository.DrugInteractionRepository;
import com.ryo.identity.repository.MergedIngredientRepository;
import com.ryo.identity.service.IDrugInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugInteractionServiceImpl implements IDrugInteractionService {

    private final DrugInteractionRepository drugInteractionRepository;
    private final MergedIngredientRepository mergedIngredientRepository;

    private MergedIngredient getIngredient(Integer id) {
        if (id == null) return null;
        return mergedIngredientRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INGREDIENT_NOT_EXIST));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public DrugInteraction create(DrugInteractionRequest req) {

        MergedIngredient ing1 = getIngredient(req.getIngredient1Id());
        MergedIngredient ing2 = getIngredient(req.getIngredient2Id());

        DrugInteraction interaction = DrugInteraction.builder()
                .mucDoNghiemTrong(req.getMucDoNghiemTrong())
                .hauQuaCuaTuongTac(req.getHauQuaCuaTuongTac())
                .coCheTuongTac(req.getCoCheTuongTac())
                .xuTriTuongTac(req.getXuTriTuongTac())
                .hoatChat1Name(req.getHoatChat1Name())
                .hoatChat2Name(req.getHoatChat2Name())
                .ingredient1(ing1)
                .ingredient2(ing2)
                .build();

        return drugInteractionRepository.save(interaction);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public DrugInteraction update(DrugInteractionRequest req, Integer id) {

        DrugInteraction interaction = drugInteractionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DRUG_INTERACTION_NOT_FOUND));

        // Load foreign keys (cho phép null)
        MergedIngredient ing1 = getIngredient(req.getIngredient1Id());
        MergedIngredient ing2 = getIngredient(req.getIngredient2Id());

        interaction.setMucDoNghiemTrong(req.getMucDoNghiemTrong());
        interaction.setHauQuaCuaTuongTac(req.getHauQuaCuaTuongTac());
        interaction.setCoCheTuongTac(req.getCoCheTuongTac());
        interaction.setXuTriTuongTac(req.getXuTriTuongTac());

        interaction.setHoatChat1Name(req.getHoatChat1Name());
        interaction.setHoatChat2Name(req.getHoatChat2Name());

        interaction.setIngredient1(ing1);
        interaction.setIngredient2(ing2);

        return drugInteractionRepository.save(interaction);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Integer id) {
        drugInteractionRepository.deleteById(id);
    }

    @Override
    public DrugInteraction get(Integer id) {
        return drugInteractionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drug interaction not found"));
    }

    private List<DrugInteraction> getByOneIngreientName(String name) {
        List<DrugInteraction> first = drugInteractionRepository.findByHoatChat1Name(name);
        List<DrugInteraction> second = drugInteractionRepository.findByHoatChat2Name(name);

        List<DrugInteraction> result = new ArrayList<>(first);
        result.addAll(second);

        return result;
    }


    @Override
    public List<DrugInteraction> getByListIngredientName(List<String> nameList) {
        if (nameList == null) {
            return List.of();
        }
        else if (nameList.size() == 1){
            return getByOneIngreientName(nameList.getFirst());
        }

        List<DrugInteraction> result = new ArrayList<>();

        for (int i = 0; i < nameList.size(); i++) {
            for (int j = i + 1; j < nameList.size(); j++) {

                String n1 = nameList.get(i);
                String n2 = nameList.get(j);

                List<DrugInteraction> interactions =
                        drugInteractionRepository.findInteractionBetween(n1, n2);

                result.addAll(interactions);
            }
        }

        return result;
    }
}
