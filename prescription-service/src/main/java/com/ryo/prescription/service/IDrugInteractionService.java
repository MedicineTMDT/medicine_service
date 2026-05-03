package com.ryo.prescription.service;

import com.ryo.prescription.dto.request.DrugInteractionRequest;
import com.ryo.prescription.entity.DrugInteraction;

import java.util.List;

public interface IDrugInteractionService {
    DrugInteraction create(DrugInteractionRequest drugInteraction);
    DrugInteraction update(DrugInteractionRequest drugInteraction, Integer id);
    void delete(Integer id);
    DrugInteraction get(Integer id);
    List<DrugInteraction> getByListIngredientName(List<String> nameList);
}
