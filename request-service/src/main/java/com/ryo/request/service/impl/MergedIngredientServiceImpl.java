package com.ryo.request.service.impl;

import com.ryo.request.dto.response.MergedIngredientResponse;
import com.ryo.request.repository.MergedIngredientRepository;
import com.ryo.request.service.IMergedIngredientService;
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
