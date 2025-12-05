package com.ryo.identity.service.impl;

import com.ryo.identity.dto.response.MergedIngredientResponse;
import com.ryo.identity.entity.MergedIngredient;
import com.ryo.identity.repository.MergedIngredientRepository;
import com.ryo.identity.service.IMergedIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MergedIngredientServiceImpl implements IMergedIngredientService {

    private final MergedIngredientRepository mergedIngredientRepository;

    @Override
    public List<MergedIngredient> suggest(String name) {
        return mergedIngredientRepository.findTop10ByNameStartingWithIgnoreCase(name);
    }
}
