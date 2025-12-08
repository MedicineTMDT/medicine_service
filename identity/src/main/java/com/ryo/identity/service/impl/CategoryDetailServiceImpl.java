package com.ryo.identity.service.impl;

import com.ryo.identity.dto.request.CategoryDetailRequest;
import com.ryo.identity.dto.response.CategoryDetailResponse;
import com.ryo.identity.dto.response.CategorySimpleResponse;
import com.ryo.identity.entity.Category;
import com.ryo.identity.entity.CategoryDetail;
import com.ryo.identity.repository.CategoryDetailRepository;
import com.ryo.identity.repository.CategoryRepository;
import com.ryo.identity.service.ICategoryDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryDetailServiceImpl implements ICategoryDetailService {

    private final CategoryDetailRepository categoryDetailRepository;
    private final CategoryRepository categoryRepository;

    private CategoryDetailResponse toCategoryDetailResponse(CategoryDetail detail) {
        return CategoryDetailResponse.builder()
                .id(detail.getId())
                .name(detail.getName())
                .content(detail.getContent())
                .created(detail.getCreated())
                .update(detail.getUpdate())
                .build();
    }

    private CategorySimpleResponse toSimple(CategoryDetail detail) {
        return CategorySimpleResponse.builder()
                .id(detail.getId())
                .name(detail.getName())
                .build();
    }

    private Category getCategory(Integer id) {
        if (id == null) return null;

        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryDetailResponse create(CategoryDetailRequest req) {

        Category category = getCategory(req.getCategoryId());

        CategoryDetail detail = CategoryDetail.builder()
                .name(req.getName())
                .content(req.getContent())
                .category(category)
                .build();

        return toCategoryDetailResponse(categoryDetailRepository.save(detail));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryDetailResponse update(Integer id, CategoryDetailRequest req) {

        CategoryDetail detail = categoryDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CategoryDetail not found: " + id));

        Category category = getCategory(req.getCategoryId());

        detail.setName(req.getName());
        detail.setContent(req.getContent());
        detail.setCategory(category);

        return toCategoryDetailResponse(categoryDetailRepository.save(detail));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Integer id) {
        if (!categoryDetailRepository.existsById(id))
            throw new RuntimeException("CategoryDetail not found: " + id);

        categoryDetailRepository.deleteById(id);
    }

    @Override
    public CategoryDetail get(Integer id) {
        return categoryDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CategoryDetail not found: " + id));
    }

    @Override
    public Page<CategorySimpleResponse> getCategoryDetailByCategoryId(Pageable pageable, Integer categoryId) {
        return categoryDetailRepository.findAllByCategory_Id(pageable, categoryId)
                .map(this::toSimple);
    }

    @Override
    public Page<CategorySimpleResponse> getCategoryDetailByCategoryDetailName(Pageable pageable, String name) {
        return categoryDetailRepository.findAllByNameContainingIgnoreCase(pageable, name)
                .map(this::toSimple);
    }

    @Override
    public Page<CategoryDetailResponse> getAll(Pageable pageable) {
        return categoryDetailRepository.findAll(pageable)
                .map(this::toCategoryDetailResponse);
    }
}
