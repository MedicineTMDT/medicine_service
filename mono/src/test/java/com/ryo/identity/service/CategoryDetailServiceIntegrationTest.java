package com.ryo.identity.service;

import com.ryo.identity.dto.request.CategoryDetailRequest;
import com.ryo.identity.dto.response.CategoryDetailResponse;
import com.ryo.identity.dto.response.CategorySimpleResponse;
import com.ryo.identity.entity.Category;
import com.ryo.identity.entity.CategoryDetail;
import com.ryo.identity.repository.CategoryDetailRepository;
import com.ryo.identity.repository.CategoryRepository;
import com.ryo.identity.service.impl.CategoryDetailServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDetailServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private CategoryDetailServiceImpl categoryDetailService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryDetailRepository categoryDetailRepository;

    @Test
    void create_shouldPersistDetailForCategory() {
        authenticateAs("admin", "ADMIN");
        Category category = saveCategory("Pain Relief", "pain-relief");

        CategoryDetailResponse result = categoryDetailService.create(CategoryDetailRequest.builder()
                .name("Paracetamol guide")
                .content("Use as directed")
                .categoryId(category.getId())
                .build());

        assertNotNull(result.getId());
        assertEquals("Paracetamol guide", result.getName());
        assertTrue(categoryDetailRepository.existsById(result.getId()));
    }

    @Test
    void update_shouldChangeDetailAndCategory() {
        authenticateAs("admin", "ADMIN");
        Category oldCategory = saveCategory("Old", "old");
        Category newCategory = saveCategory("New", "new");
        CategoryDetail detail = categoryDetailRepository.save(CategoryDetail.builder()
                .name("Old detail")
                .content("Old content")
                .category(oldCategory)
                .build());

        CategoryDetailResponse result = categoryDetailService.update(detail.getId(), CategoryDetailRequest.builder()
                .name("New detail")
                .content("New content")
                .categoryId(newCategory.getId())
                .build());

        assertEquals("New detail", result.getName());
        assertEquals(newCategory.getId(), categoryDetailRepository.findById(detail.getId()).orElseThrow()
                .getCategory().getId());
    }

    @Test
    void getCategoryDetailByCategoryId_shouldReturnOnlyThatCategory() {
        Category category = saveCategory("Pain", "pain");
        Category other = saveCategory("Vitamin", "vitamin");
        categoryDetailRepository.save(CategoryDetail.builder().name("Aspirin").content("A").category(category).build());
        categoryDetailRepository.save(CategoryDetail.builder().name("Vitamin C").content("C").category(other).build());

        Page<CategorySimpleResponse> result =
                categoryDetailService.getCategoryDetailByCategoryId(PageRequest.of(0, 10), category.getId());

        assertEquals(1, result.getTotalElements());
        assertEquals("Aspirin", result.getContent().getFirst().getName());
    }

    @Test
    void getCategoryDetailByCategoryDetailName_shouldSearchIgnoreCase() {
        categoryDetailRepository.save(CategoryDetail.builder().name("Cough syrup").content("A").build());
        categoryDetailRepository.save(CategoryDetail.builder().name("Eye drops").content("B").build());

        Page<CategorySimpleResponse> result =
                categoryDetailService.getCategoryDetailByCategoryDetailName(PageRequest.of(0, 10), "COUGH");

        assertEquals(1, result.getTotalElements());
        assertEquals("Cough syrup", result.getContent().getFirst().getName());
    }

    @Test
    void delete_shouldRemoveDetail() {
        authenticateAs("admin", "ADMIN");
        CategoryDetail detail = categoryDetailRepository.save(CategoryDetail.builder()
                .name("Delete")
                .content("Delete")
                .build());

        categoryDetailService.delete(detail.getId());

        assertFalse(categoryDetailRepository.existsById(detail.getId()));
    }

    private Category saveCategory(String name, String slug) {
        return categoryRepository.save(Category.builder()
                .name(name)
                .slug(slug)
                .amount(0)
                .build());
    }
}
