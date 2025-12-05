package com.ryo.identity.service;

import com.ryo.identity.dto.response.MergedIngredientResponse;
import com.ryo.identity.entity.MergedIngredient;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IMergedIngredientService {
    List<MergedIngredient> suggest(String name);
}
