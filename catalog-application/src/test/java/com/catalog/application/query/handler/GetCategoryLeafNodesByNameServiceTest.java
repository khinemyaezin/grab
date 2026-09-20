package com.catalog.application.query.handler;

import com.catalog.application.service.GetCategoryLeafNodesByNameService;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.readmodel.CategoryView;
import com.catalog.application.query.CategoryLeavesResult;
import com.catalog.application.query.GetCategoryLeafNodesByNameQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCategoryLeafNodesByNameServiceTest {

    @Mock
    private CategoryQueryPort categoryQueryRepository;

    private GetCategoryLeafNodesByNameService service;

    @BeforeEach
    void setUp() {
        service = new GetCategoryLeafNodesByNameService(categoryQueryRepository);
    }

    @Test
    void handle_mapsLeafNodesFromQueryRepository() {
        when(categoryQueryRepository.findLeafNodesByName("elect")).thenReturn(List.of(
                new CategoryView("cat-4", "Android Phones", "cat-2", true, true, true)
        ));

        CategoryLeavesResult result = service.execute(new GetCategoryLeafNodesByNameQuery("elect"));

        assertThat(result.leaves()).singleElement()
                .satisfies(leaf -> {
                    assertThat(leaf.id()).isEqualTo("cat-4");
                    assertThat(leaf.name()).isEqualTo("Android Phones");
                    assertThat(leaf.parentId()).isEqualTo("cat-2");
                });
    }
}
