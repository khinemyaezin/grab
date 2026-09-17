package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryNodeView;
import com.grab.store.catalog.queries.GetStorefrontCategoryTreeQuery;
import com.grab.store.catalog.queries.StorefrontCategoryNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStorefrontCategoryTreeQueryHandlerTest {

    @Mock
    private CategoryQueryRepository categoryQueryRepository;

    private GetStorefrontCategoryTreeQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetStorefrontCategoryTreeQueryHandler(categoryQueryRepository);
    }

    @Test
    void handle_withoutRootId_returnsAllRootTrees() {
        when(categoryQueryRepository.findRootTrees()).thenReturn(List.of(
                new CategoryNodeView("cat-1", "Electronics", null, List.of())
        ));

        List<StorefrontCategoryNode> trees = handler.handle(new GetStorefrontCategoryTreeQuery(null));

        assertThat(trees).extracting(StorefrontCategoryNode::id).containsExactly("cat-1");
    }

    @Test
    void handle_withRootId_wrapsSingleTree() {
        when(categoryQueryRepository.findTree("cat-1")).thenReturn(Optional.of(
                new CategoryNodeView("cat-1", "Electronics", null, List.of())
        ));

        List<StorefrontCategoryNode> trees = handler.handle(new GetStorefrontCategoryTreeQuery("cat-1"));

        assertThat(trees).singleElement().extracting(StorefrontCategoryNode::id).isEqualTo("cat-1");
    }
}
