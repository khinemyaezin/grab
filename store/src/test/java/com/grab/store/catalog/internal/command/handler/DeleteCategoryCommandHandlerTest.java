package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.DeleteCategoryCommand;
import com.grab.store.catalog.internal.command.DeleteCategoryResult;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
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
class DeleteCategoryCommandHandlerTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;

    private DeleteCategoryCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeleteCategoryCommandHandler(categoryRepository, productRepository);
    }

    @Test
    void handle_blocksDeleteWhenSubtreeHasAssignedProducts() {
        Id categoryId = new CommonId("category-123");
        Category category = Category.createRoot(categoryId, "Category");

        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findSubtreeIds(categoryId)).thenReturn(Set.of(categoryId));
        when(productRepository.existsByCategoryIds(Set.of(categoryId))).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(new DeleteCategoryCommand(categoryId)))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.category.has_assigned_products");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }

    @Test
    void handle_deletesWhenSubtreeHasNoAssignedProducts() {
        Id categoryId = new CommonId("category-123");
        Category category = Category.createRoot(categoryId, "Category");

        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findSubtreeIds(categoryId)).thenReturn(Set.of(categoryId));
        when(productRepository.existsByCategoryIds(Set.of(categoryId))).thenReturn(false);

        DeleteCategoryResult result = handler.handle(new DeleteCategoryCommand(categoryId));

        verify(categoryRepository).deleteCascade(category);
        assertThat(result.deleted()).isTrue();
    }

    @Test
    void handle_missingCategoryReturnsFalse() {
        Id categoryId = new CommonId("missing-category");
        when(categoryRepository.find(categoryId)).thenReturn(Optional.empty());

        DeleteCategoryResult result = handler.handle(new DeleteCategoryCommand(categoryId));

        assertThat(result.deleted()).isFalse();
    }
}
