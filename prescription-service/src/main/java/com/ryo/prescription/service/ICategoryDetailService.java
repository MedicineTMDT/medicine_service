package com.ryo.prescription.service;

import com.ryo.prescription.dto.request.CategoryDetailRequest;
import com.ryo.prescription.dto.response.CategoryDetailResponse;
import com.ryo.prescription.dto.response.CategorySimpleResponse;
import com.ryo.prescription.entity.CategoryDetail;
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
