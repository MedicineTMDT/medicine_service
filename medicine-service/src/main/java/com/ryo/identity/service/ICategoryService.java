package com.ryo.identity.service;

import com.ryo.identity.dto.request.CategoryRequest;
import com.ryo.identity.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

public interface ICategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Integer id, CategoryRequest request);

    void delete(Integer id);

    CategoryResponse get(Integer id);

    Page<CategoryResponse> getAll(Pageable pageable);

    Page<CategoryResponse> getByName(Pageable pageable, String name);
}
