package com.catalog.application.query.handler;

import com.catalog.application.service.GetCategoryParentService;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.readmodel.CategoryView;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.query.CategoryResult;
import com.catalog.application.query.GetCategoryParentQuery;
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
class GetCategoryParentServiceTest {

    @Mock
    private CategoryQueryPort categoryQueryRepository;

    private GetCategoryParentService service;

    @BeforeEach
    void setUp() {
        service = new GetCategoryParentService(categoryQueryRepository);
    }

    @Test
    void handle_mapsParentFromQueryRepository() {
        when(categoryQueryRepository.exists("cat-2")).thenReturn(true);
        when(categoryQueryRepository.findParent("cat-2")).thenReturn(Optional.of(
                new CategoryView("cat-1", "Electronics", null, true, true, true)
        ));

        CategoryResult result = service.execute(new GetCategoryParentQuery("cat-2"));

        assertThat(result.id()).isEqualTo("cat-1");
        assertThat(result.name()).isEqualTo("Electronics");
    }

    @Test
    void handle_missingCategoryThrows() {
        when(categoryQueryRepository.exists("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.execute(new GetCategoryParentQuery("missing")))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.not_found"));
    }

    @Test
    void handle_missingParentThrows() {
        when(categoryQueryRepository.exists("cat-1")).thenReturn(true);
        when(categoryQueryRepository.findParent("cat-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetCategoryParentQuery("cat-1")))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.parent_not_found_for_category"));
    }
}
