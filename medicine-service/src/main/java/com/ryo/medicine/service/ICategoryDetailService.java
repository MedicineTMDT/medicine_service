package com.ryo.medicine.service;

import com.ryo.medicine.dto.request.CategoryDetailRequest;
import com.ryo.medicine.dto.response.CategoryDetailResponse;
import com.ryo.medicine.dto.response.CategorySimpleResponse;
import com.ryo.medicine.entity.CategoryDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICategoryDetailService {

    CategoryDetailResponse create (CategoryDetailRequest request);

    CategoryDetailResponse update (Integer id, CategoryDetailRequest request);

    void delete(Integer id);

    CategoryDetail get(Integer id);

    Page<CategorySimpleResponse> getCategoryDetailByCategoryId(Pageable pageable, Integer categoryId);

    Page<CategorySimpleResponse> getCategoryDetailByCategoryDetailName(Pageable pageable, String name);

    Page<CategoryDetailResponse> getAll(Pageable pageable);

}
