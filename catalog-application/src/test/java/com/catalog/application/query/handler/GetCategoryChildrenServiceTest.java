package com.catalog.application.query.handler;

import com.catalog.application.service.GetCategoryChildrenService;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.readmodel.CategoryChildrenView;
import com.catalog.application.readmodel.CategoryView;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.query.CategoryChildrenResult;
import com.catalog.application.query.GetCategoryChildrenQuery;
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
class GetCategoryChildrenServiceTest {

    @Mock
    private CategoryQueryPort categoryQueryRepository;

    private GetCategoryChildrenService service;

    @BeforeEach
    void setUp() {
        service = new GetCategoryChildrenService(categoryQueryRepository);
    }

    @Test
    void handle_mapsChildrenFromQueryRepository() {
        when(categoryQueryRepository.findChildren("cat-1")).thenReturn(Optional.of(
                new CategoryChildrenView(
                        "cat-1",
                        List.of(new CategoryView("cat-2", "Smartphones", "cat-1", true, true, true))
                )
        ));

        CategoryChildrenResult result = service.execute(new GetCategoryChildrenQuery("cat-1"));

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

        assertThatThrownBy(() -> service.execute(new GetCategoryChildrenQuery("missing")))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.category.not_found"));
    }
}
