package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryChildrenView;
import com.catalog.infrastructure.view.CategoryView;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.CategoryChildrenResult;
import com.grab.store.catalog.internal.query.GetCategoryChildrenQuery;
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
class GetCategoryChildrenQueryHandlerTest {

    @Mock
    private CategoryQueryRepository categoryQueryRepository;

    private GetCategoryChildrenQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetCategoryChildrenQueryHandler(categoryQueryRepository);
    }

    @Test
    void handle_mapsChildrenFromQueryRepository() {
        when(categoryQueryRepository.findChildren("cat-1")).thenReturn(Optional.of(
                new CategoryChildrenView(
                        "cat-1",
                        List.of(new CategoryView("cat-2", "Smartphones", "cat-1", true, true, true))
                )
        ));

        CategoryChildrenResult result = handler.handle(new GetCategoryChildrenQuery("cat-1"));

        assertThat(result.parentId()).isEqualTo("cat-1");
        assertThat(result.children()).singleElement()
                .satisfies(child -> {
                    assertThat(child.id()).isEqualTo("cat-2");
                    assertThat(child.parentId()).isEqualTo("cat-1");
                });
    }

    @Test
    void handle_missingCategoryThrows() {
        when(categoryQueryRepository.findChildren("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetCategoryChildrenQuery("missing")))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.not_found"));
    }
}
