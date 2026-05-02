package com.ryo.medicine.service;

import com.ryo.medicine.dto.response.MergedIngredientResponse;

import java.util.List;

public interface IMergedIngredientService {
    List<MergedIngredientResponse> suggest(String name);
}
