package com.ryo.medicine.service.impl;

import com.ryo.medicine.dto.response.MergedIngredientResponse;
import com.ryo.medicine.repository.MergedIngredientRepository;
import com.ryo.medicine.service.IMergedIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MergedIngredientServiceImpl implements IMergedIngredientService {

    private final MergedIngredientRepository mergedIngredientRepository;

    @Override
    public List<MergedIngredientResponse> suggest(String name) {
        return mergedIngredientRepository.findTop10ByNameStartingWithIgnoreCase(name)
                .stream().map(
                    item -> MergedIngredientResponse.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .build()
                ).toList();
    }
}
