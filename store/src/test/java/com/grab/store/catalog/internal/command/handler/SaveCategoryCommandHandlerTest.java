package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.SaveCategoryCommand;
import com.grab.store.catalog.internal.command.SaveCategoryResult;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveCategoryCommandHandlerTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private IdGenerator idGenerator;

    @Captor
    private ArgumentCaptor<Category> categoryCaptor;

    private SaveCategoryCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SaveCategoryCommandHandler(categoryRepository, idGenerator);
    }

    @Test
    void handle_rootCategoryCreatesDefaultRootPolicy() {
        Id generatedId = new CommonId("category-root");
        when(idGenerator.generateId()).thenReturn(generatedId);

        SaveCategoryResult result = handler.handle(new SaveCategoryCommand(
                "Electronics",
                null,
                false,
                true,
                true,
                true
        ));

        verify(categoryRepository).save(categoryCaptor.capture());
        Category saved = categoryCaptor.getValue();

        assertThat(saved.isRoot()).isTrue();
        assertThat(saved.getName()).isEqualTo("Electronics");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.isListingAllowed()).isFalse();
        assertThat(saved.isReviewRequired()).isFalse();
        assertThat(saved.isC2cAllowed()).isFalse();
        assertThat(result.categoryId()).isEqualTo("category-root");
    }

    @Test
    void handle_childCategoryUsesParentAndRequestedFlags() {
        Id generatedId = new CommonId("category-child");
        Id parentId = new CommonId("category-parent");
        Category parent = Category.createRoot(parentId, "Parent");

        when(idGenerator.generateId()).thenReturn(generatedId);
        when(categoryRepository.find(parentId)).thenReturn(Optional.of(parent));

        SaveCategoryResult result = handler.handle(new SaveCategoryCommand(
                "Cameras",
                parentId,
                true,
                true,
                true,
                false
        ));

        verify(categoryRepository).save(categoryCaptor.capture());
        Category saved = categoryCaptor.getValue();

        assertThat(saved.isRoot()).isFalse();
        assertThat(saved.getParentId()).contains(parentId);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.isListingAllowed()).isTrue();
        assertThat(saved.isReviewRequired()).isTrue();
        assertThat(saved.isC2cAllowed()).isFalse();
        assertThat(result.categoryId()).isEqualTo("category-child");
    }

    @Test
    void handle_missingParentThrows() {
        Id parentId = new CommonId("missing-parent");
        when(idGenerator.generateId()).thenReturn(new CommonId("category-child"));
        when(categoryRepository.find(parentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new SaveCategoryCommand(
                "Cameras",
                parentId,
                true,
                true,
                false,
                false
        )))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.parent_not_found"));
    }
}
