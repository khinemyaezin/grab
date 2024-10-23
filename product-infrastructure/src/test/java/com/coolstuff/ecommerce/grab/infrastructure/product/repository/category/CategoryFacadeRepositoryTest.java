package com.coolstuff.ecommerce.grab.infrastructure.product.repository.category;

import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.integration.category.CategoryService;
import com.product.infrastructure.integration.category.CategoryTreeBuilder;
import com.product.domain.entity.category.CategoryComposite;
import com.product.domain.entity.category.CategoryLeaf;
import com.product.infrastructure.integration.category.NodeComponentFactory;
import com.product.infrastructure.repository.category.CategoryFacadeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryFacadeRepositoryTest {
    private TreeBuilder treeBuilder;

    @BeforeEach
    void mockClasses() {
        var nodeComponentFactory = mock(NodeComponentFactory.class);
        when(nodeComponentFactory.createCompositeNodeComponent()).thenAnswer(o -> new CategoryComposite());
        when(nodeComponentFactory.createLeafNodeComponent()).thenAnswer(o -> new CategoryLeaf());
        treeBuilder = new CategoryTreeBuilder(nodeComponentFactory);
    }

    @Test
    void shouldCastAbstractCategoryClass_whenFindingImmediateSubordinatesOf() {
        var categoryService = mock(CategoryService.class);
        when(categoryService.findImmediateCategory("1")).thenReturn(getCategoryEntities());

        var categoryRepository = new CategoryFacadeRepository(categoryService, null, null, treeBuilder, null);
        var categoryOptional = categoryRepository.findImmediateChildrenOf("1");

        Assertions.assertTrue(categoryOptional.isPresent());
        Assertions.assertInstanceOf(CategoryComposite.class, categoryOptional.get());
        Assertions.assertEquals(1, categoryOptional.get().getLft());

        Assertions.assertEquals(2, categoryOptional.get().getChildren().size());
        categoryOptional.get().getChildren().forEach(c -> Assertions.assertInstanceOf(CategoryLeaf.class, c));
    }

    private List<CategoryEntity> getCategoryEntities() {
        CategoryEntity c1 = new CategoryEntity();
        c1.setName("Electronics");
        c1.setLft(1);
        c1.setRgt(6);
        c1.setDepth(1);

        CategoryEntity c1_1 = new CategoryEntity();
        c1_1.setName("Laptops");
        c1_1.setLft(2);
        c1_1.setRgt(3);
        c1_1.setDepth(2);

        CategoryEntity c1_2 = new CategoryEntity();
        c1_2.setName("Mobile Phones");
        c1_2.setLft(4);
        c1_2.setRgt(5);
        c1_2.setDepth(2);

        return List.of(c1, c1_1, c1_2);
    }

}