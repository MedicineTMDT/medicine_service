package com.ryo.medicine.service;

import com.ryo.medicine.dto.request.DrugRequest;
import com.ryo.medicine.dto.response.DrugResponse;
import com.ryo.medicine.dto.response.DrugSimpleResponse;
import com.ryo.medicine.entity.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IDrugService {
    DrugResponse create (DrugRequest drugRequest);
    DrugResponse update(DrugRequest drugRequest, Integer id);
    void delete(Integer id);
    Drug get(Integer id);

    Page<DrugSimpleResponse> getAll(Pageable pageable);
    Page<DrugSimpleResponse> getAllByCategoryId(Pageable pageable, Integer id);
    Page<DrugSimpleResponse> getAllByDrugName(Pageable pageable, String name);

    List<String> getDrugIngredients(Integer drugId);
    List<DrugSimpleResponse> getTop10ByNameStartingWithIgnoreCase(String name);



}
