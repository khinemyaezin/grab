package com.catalog.application.command.handler;

import com.catalog.application.service.DeleteCategoryService;

import com.catalog.domain.aggregate.Category;
import com.catalog.application.port.outbound.CategoryHierarchyQueryPort;
import com.catalog.domain.port.outbound.CategoryHierarchyRepository;
import com.catalog.domain.port.outbound.CategoryRepository;
import com.catalog.domain.port.outbound.ProductRepository;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.catalog.application.model.write.DeleteCategoryCommand;
import com.catalog.application.model.write.DeleteCategoryResult;
import com.catalog.application.exception.CatalogServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryHierarchyQueryPort categoryHierarchyQueryPort;
    @Mock
    private CategoryHierarchyRepository categoryHierarchyRepository;
    @Mock
    private ProductRepository productRepository;

    private DeleteCategoryService service;

    @BeforeEach
    void setUp() {
        service = new DeleteCategoryService(categoryRepository, categoryHierarchyQueryPort, categoryHierarchyRepository, productRepository);
    }

    @Test
    void execute_subtreeHasAssignedProducts_throwsException() {
        Id categoryId = new CommonId("category-123");
        Category category = Category.createRoot(categoryId, "Category");

        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(category));
        when(categoryHierarchyQueryPort.findSubtreeIds(categoryId)).thenReturn(Set.of(categoryId));
        when(productRepository.existsByCategoryIds(Set.of(categoryId))).thenReturn(true);

        assertThatThrownBy(() -> service.execute(new DeleteCategoryCommand(categoryId)))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.category.has_assigned_products");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }

    @Test
    void execute_subtreeHasNoAssignedProducts_deletesCategory() {
        Id categoryId = new CommonId("category-123");
        Category category = Category.createRoot(categoryId, "Category");

        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(category));
        when(categoryHierarchyQueryPort.findSubtreeIds(categoryId)).thenReturn(Set.of(categoryId));
        when(productRepository.existsByCategoryIds(Set.of(categoryId))).thenReturn(false);

        DeleteCategoryResult result = service.execute(new DeleteCategoryCommand(categoryId));

        verify(categoryHierarchyRepository).deleteSubtree(categoryId);
        assertThat(result.deleted()).isTrue();
    }

    @Test
    void execute_missingCategory_returnsFalse() {
        Id categoryId = new CommonId("missing-category");
        when(categoryRepository.find(categoryId)).thenReturn(Optional.empty());

        DeleteCategoryResult result = service.execute(new DeleteCategoryCommand(categoryId));

        assertThat(result.deleted()).isFalse();
    }
}
