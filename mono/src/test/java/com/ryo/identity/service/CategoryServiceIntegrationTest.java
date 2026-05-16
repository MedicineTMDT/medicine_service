package com.ryo.identity.service;

import com.ryo.identity.dto.request.CategoryRequest;
import com.ryo.identity.dto.response.CategoryResponse;
import com.ryo.identity.entity.Category;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.repository.CategoryRepository;
import com.ryo.identity.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void create_shouldPersistCategoryWithAmountZero() {
        authenticateAs("admin", "ADMIN");

        CategoryResponse result = categoryService.create(CategoryRequest.builder()
                .name("Pain Relief")
                .slug("pain-relief")
                .build());

        assertNotNull(result.getId());
        assertEquals("Pain Relief", result.getName());
        assertEquals(0, result.getAmount());
        assertTrue(categoryRepository.existsBySlug("pain-relief"));
    }

    @Test
    void create_whenSlugExists_shouldThrowAppException() {
        authenticateAs("admin", "ADMIN");
        categoryRepository.save(Category.builder()
                .name("Existing")
                .slug("existing")
                .amount(0)
                .build());

        assertThrows(AppException.class, () -> categoryService.create(CategoryRequest.builder()
                .name("Other")
                .slug("existing")
                .build()));
    }

    @Test
    void update_shouldChangeNameAndSlug() {
        authenticateAs("admin", "ADMIN");
        Category category = categoryRepository.save(Category.builder()
                .name("Old")
                .slug("old")
                .amount(0)
                .build());

        CategoryResponse result = categoryService.update(category.getId(), CategoryRequest.builder()
                .name("New")
                .slug("new")
                .build());

        assertEquals("New", result.getName());
        assertEquals("new", result.getSlug());
    }

    @Test
    void getByName_shouldReturnMatchingCategories() {
        categoryRepository.save(Category.builder().name("Antibiotic").slug("antibiotic").amount(0).build());
        categoryRepository.save(Category.builder().name("Vitamin").slug("vitamin").amount(0).build());

        Page<CategoryResponse> result = categoryService.getByName(PageRequest.of(0, 10), "anti");

        assertEquals(1, result.getTotalElements());
        assertEquals("Antibiotic", result.getContent().getFirst().getName());
    }

    @Test
    void delete_shouldRemoveCategory() {
        authenticateAs("admin", "ADMIN");
        Category category = categoryRepository.save(Category.builder()
                .name("Delete Me")
                .slug("delete-me")
                .amount(0)
                .build());

        categoryService.delete(category.getId());

        assertFalse(categoryRepository.existsById(category.getId()));
    }
}
