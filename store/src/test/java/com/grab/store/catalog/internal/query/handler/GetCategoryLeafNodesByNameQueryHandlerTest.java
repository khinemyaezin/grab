package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryView;
import com.grab.store.catalog.internal.query.CategoryLeavesResult;
import com.grab.store.catalog.internal.query.GetCategoryLeafNodesByNameQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCategoryLeafNodesByNameQueryHandlerTest {

    @Mock
    private CategoryQueryRepository categoryQueryRepository;

    private GetCategoryLeafNodesByNameQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetCategoryLeafNodesByNameQueryHandler(categoryQueryRepository);
    }

    @Test
    void handle_mapsLeafNodesFromQueryRepository() {
        when(categoryQueryRepository.findLeafNodesByName("elect")).thenReturn(List.of(
                new CategoryView("cat-4", "Android Phones", "cat-2", true, true, false, true)
        ));

        CategoryLeavesResult result = handler.handle(new GetCategoryLeafNodesByNameQuery("elect"));

        assertThat(result.leaves()).singleElement()
                .satisfies(leaf -> {
                    assertThat(leaf.id()).isEqualTo("cat-4");
                    assertThat(leaf.name()).isEqualTo("Android Phones");
                    assertThat(leaf.parentId()).isEqualTo("cat-2");
                });
    }
}
