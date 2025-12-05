package com.ryo.identity.service;

import com.ryo.identity.dto.request.DrugInteractionRequest;
import com.ryo.identity.entity.DrugInteraction;

import java.util.List;

public interface IDrugInteractionService {
    DrugInteraction create(DrugInteractionRequest drugInteraction);
    DrugInteraction update(DrugInteractionRequest drugInteraction, Integer id);
    void delete(Integer id);
    DrugInteraction get(Integer id);
    List<DrugInteraction> getByListIngredientName(List<String> nameList);
}
