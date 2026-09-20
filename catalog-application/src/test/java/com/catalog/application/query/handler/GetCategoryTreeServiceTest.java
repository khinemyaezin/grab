package com.catalog.application.query.handler;

import com.catalog.application.service.GetCategoryTreeService;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.model.read.CategoryNodeView;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.model.read.CategoryNodeResult;
import com.catalog.application.model.read.GetCategoryTreeQuery;
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
class GetCategoryTreeServiceTest {

    @Mock
    private CategoryQueryPort categoryQueryRepository;

    private GetCategoryTreeService service;

    @BeforeEach
    void setUp() {
        service = new GetCategoryTreeService(categoryQueryRepository);
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

        CategoryNodeResult result = service.execute(new GetCategoryTreeQuery("cat-1"));

        assertThat(result.id()).isEqualTo("cat-1");
        assertThat(result.children()).singleElement()
                .extracting(CategoryNodeResult::id)
                .isEqualTo("cat-2");
    }

    @Test
    void handle_missingCategoryThrows() {
        when(categoryQueryRepository.findTree("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetCategoryTreeQuery("missing")))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.not_found"));
    }
}
