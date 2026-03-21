package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.impl.CategoryQueryRepositoryImpl;
import com.catalog.infrastructure.view.CategoryTreeNode;
import com.catalog.infrastructure.view.CategoryChildrenView;
import com.catalog.infrastructure.view.CategoryNodeView;
import com.catalog.infrastructure.view.CategoryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryQueryRepositoryTest {

    private CategoryJpaRepo categoryJpaRepo;
    private CategoryNodeRepository categoryNodeRepository;
    private CategoryQueryRepository repository;

    @BeforeEach
    void setUp() {
        categoryJpaRepo = mock(CategoryJpaRepo.class);
        categoryNodeRepository = mock(CategoryNodeRepository.class);
        repository = new CategoryQueryRepositoryImpl(categoryJpaRepo, categoryNodeRepository);
    }

    @Test
    void exists_returnsTrueWhenCategoryIsPresent() {
        when(categoryJpaRepo.findByUuid("cat-1")).thenReturn(Optional.of(entity("cat-1", "Electronics")));

        assertThat(repository.exists("cat-1")).isTrue();
    }

    @Test
    void findTree_mapsNestedNodeRecursively() {
        CategoryTreeNode tree = new CategoryTreeNode(
                entity("cat-1", "Electronics"),
                null,
                List.of(new CategoryTreeNode(entity("cat-2", "Smartphones"), "cat-1", List.of()))
        );
        when(categoryNodeRepository.findSubtree("cat-1")).thenReturn(Optional.of(tree));

        Optional<CategoryNodeView> result = repository.findTree("cat-1");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().id()).isEqualTo("cat-1");
        assertThat(result.orElseThrow().children()).singleElement()
                .extracting(CategoryNodeView::id)
                .isEqualTo("cat-2");
    }

    @Test
    void findChildren_mapsImmediateChildrenWithParentId() {
        CategoryEntity parent = entity("cat-1", "Electronics");
        CategoryEntity child = entity("cat-2", "Smartphones");

        when(categoryJpaRepo.findByUuid("cat-1")).thenReturn(Optional.of(parent));
        when(categoryNodeRepository.findImmediateChildren("cat-1")).thenReturn(List.of(child));

        Optional<CategoryChildrenView> result = repository.findChildren("cat-1");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().parentId()).isEqualTo("cat-1");
        assertThat(result.orElseThrow().children()).singleElement()
                .satisfies(category -> {
                    assertThat(category.id()).isEqualTo("cat-2");
                    assertThat(category.parentId()).isEqualTo("cat-1");
                });
    }

    @Test
    void findParent_mapsParentAndGrandparentId() {
        CategoryEntity parent = entity("cat-2", "Smartphones");
        CategoryEntity grandParent = entity("cat-1", "Electronics");

        when(categoryNodeRepository.findParent("cat-3")).thenReturn(Optional.of(parent));
        when(categoryNodeRepository.findParent(parent)).thenReturn(Optional.of(grandParent));

        Optional<CategoryView> result = repository.findParent("cat-3");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().id()).isEqualTo("cat-2");
        assertThat(result.orElseThrow().parentId()).isEqualTo("cat-1");
    }

    @Test
    void findChildren_returnsEmptyWhenCategoryMissing() {
        when(categoryJpaRepo.findByUuid("missing")).thenReturn(Optional.empty());

        assertThat(repository.findChildren("missing")).isEmpty();
    }

    @Test
    void findLeafNodesByName_mapsLeafNodesWithParentId() {
        CategoryEntity parent = entity("cat-1", "Electronics");
        CategoryEntity leaf = entity("cat-4", "Android Phones");

        when(categoryNodeRepository.findLeafNodeByName("Elect")).thenReturn(List.of(leaf));
        when(categoryNodeRepository.findParent(leaf)).thenReturn(Optional.of(parent));

        List<CategoryView> result = repository.findLeafNodesByName("Elect");

        assertThat(result).singleElement()
                .satisfies(category -> {
                    assertThat(category.id()).isEqualTo("cat-4");
                    assertThat(category.parentId()).isEqualTo("cat-1");
                    assertThat(category.name()).isEqualTo("Android Phones");
                });
    }

    private CategoryEntity entity(String uuid, String name) {
        CategoryEntity entity = new CategoryEntity();
        entity.setUuid(uuid);
        entity.setName(name);
        entity.setActive(true);
        entity.setListingAllowed(true);
        entity.setReviewRequired(false);
        entity.setC2cAllowed(true);
        return entity;
    }
}
