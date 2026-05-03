package com.ryo.request.service;

import com.ryo.request.dto.response.MergedIngredientResponse;

import java.util.List;

public interface IMergedIngredientService {
    List<MergedIngredientResponse> suggest(String name);
}
