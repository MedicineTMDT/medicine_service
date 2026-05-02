package com.ryo.identity.service;

import com.ryo.identity.dto.response.MergedIngredientResponse;

import java.util.List;

public interface IMergedIngredientService {
    List<MergedIngredientResponse> suggest(String name);
}
