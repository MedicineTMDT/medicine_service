package com.ryo.medicine.service.impl;

import com.ryo.medicine.dto.request.CategoryRequest;
import com.ryo.medicine.dto.response.CategoryResponse;
import com.ryo.medicine.entity.Category;
import com.ryo.medicine.exception.AppException;
import com.ryo.medicine.exception.ErrorCode;
import com.ryo.medicine.repository.CategoryRepository;
import com.ryo.medicine.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

@Service @Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryResponse create(CategoryRequest request) {

        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.SLUG_EXISTS);
        }

        Category category = Category.builder()
                .name(request.getName())
                .amount(0)
                .slug(request.getSlug())
                .build();

        categoryRepository.save(category);

        return mapToResponse(category);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryResponse update(Integer id, CategoryRequest request) {
        log.info("begin update");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        log.info("User Name: " + authentication.getName());
        log.info("User Authorities (Roles): " + authentication.getAuthorities());
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        category.setName(request.getName());

        if (!category.getSlug().equals(request.getSlug()) &&
                categoryRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.SLUG_EXISTS);
        }

        category.setSlug(request.getSlug());
        categoryRepository.save(category);

        return mapToResponse(category);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Integer id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponse get(Integer id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return mapToResponse(category);
    }

    @Override
    public Page<CategoryResponse> getAll(Pageable pageable) {

        return categoryRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<CategoryResponse> getByName(Pageable pageable, String name){
        return categoryRepository.findByNameContainingIgnoreCase(name,pageable)
                .map(this::mapToResponse);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .amount(category.getAmount())
                .slug(category.getSlug())
                .created(category.getCreated())
                .build();
    }
}
