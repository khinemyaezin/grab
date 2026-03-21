package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryNodeView;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.CategoryNodeResult;
import com.grab.store.catalog.internal.query.GetCategoryTreeQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCategoryTreeQueryHandlerTest {

    @Mock
    private CategoryQueryRepository categoryQueryRepository;

    private GetCategoryTreeQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetCategoryTreeQueryHandler(categoryQueryRepository);
    }

    @Test
    void handle_mapsTreeFromQueryRepository() {
        when(categoryQueryRepository.findTree("cat-1")).thenReturn(Optional.of(
                new CategoryNodeView(
                        "cat-1",
                        "Electronics",
                        null,
                        List.of(new CategoryNodeView("cat-2", "Smartphones", "cat-1", List.of()))
                )
        ));

        CategoryNodeResult result = handler.handle(new GetCategoryTreeQuery("cat-1"));

        assertThat(result.id()).isEqualTo("cat-1");
        assertThat(result.children()).singleElement()
                .extracting(CategoryNodeResult::id)
                .isEqualTo("cat-2");
    }

    @Test
    void handle_missingCategoryThrows() {
        when(categoryQueryRepository.findTree("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetCategoryTreeQuery("missing")))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.not_found"));
    }
}
