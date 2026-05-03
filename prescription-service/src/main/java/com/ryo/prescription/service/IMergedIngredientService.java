package com.ryo.prescription.service;

import com.ryo.prescription.dto.response.MergedIngredientResponse;

import java.util.List;

public interface IMergedIngredientService {
    List<MergedIngredientResponse> suggest(String name);
}
