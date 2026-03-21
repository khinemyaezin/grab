package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryView;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.CategoryResult;
import com.grab.store.catalog.internal.query.GetCategoryParentQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCategoryParentQueryHandlerTest {

    @Mock
    private CategoryQueryRepository categoryQueryRepository;

    private GetCategoryParentQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetCategoryParentQueryHandler(categoryQueryRepository);
    }

    @Test
    void handle_mapsParentFromQueryRepository() {
        when(categoryQueryRepository.exists("cat-2")).thenReturn(true);
        when(categoryQueryRepository.findParent("cat-2")).thenReturn(Optional.of(
                new CategoryView("cat-1", "Electronics", null, true, true, false, true)
        ));

        CategoryResult result = handler.handle(new GetCategoryParentQuery("cat-2"));

        assertThat(result.id()).isEqualTo("cat-1");
        assertThat(result.name()).isEqualTo("Electronics");
    }

    @Test
    void handle_missingCategoryThrows() {
        when(categoryQueryRepository.exists("missing")).thenReturn(false);

        assertThatThrownBy(() -> handler.handle(new GetCategoryParentQuery("missing")))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.not_found"));
    }

    @Test
    void handle_missingParentThrows() {
        when(categoryQueryRepository.exists("cat-1")).thenReturn(true);
        when(categoryQueryRepository.findParent("cat-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetCategoryParentQuery("cat-1")))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.parent_not_found_for_category"));
    }
}
