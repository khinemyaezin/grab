package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.view.CategoryChildrenView;
import com.catalog.infrastructure.view.CategoryNodeView;
import com.catalog.infrastructure.view.CategoryView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryQueryRepositoryTest extends CategoryRepositoryTestConfig {

    @Autowired
    private CategoryNodeRepository categoryNodeRepository;

    @Autowired
    private CategoryQueryRepository categoryQueryRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        CategoryEntity electronics = category("cat-1", "Electronics");
        CategoryEntity smartphones = category("cat-2", "Smartphones");
        CategoryEntity laptops = category("cat-3", "Laptops");

        categoryNodeRepository.insert(electronics, null);
        categoryNodeRepository.insert(smartphones, "cat-1");
        categoryNodeRepository.insert(laptops, "cat-1");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findTree_returnsNestedHierarchy() {
        Optional<CategoryNodeView> tree = categoryQueryRepository.findTree("cat-1");

        assertThat(tree).isPresent();
        assertThat(tree.orElseThrow().children())
                .extracting(CategoryNodeView::id)
                .containsExactlyInAnyOrder("cat-2", "cat-3");
    }

    @Test
    void findChildren_returnsImmediateChildren() {
        Optional<CategoryChildrenView> children = categoryQueryRepository.findChildren("cat-1");

        assertThat(children).isPresent();
        assertThat(children.orElseThrow().parentId()).isEqualTo("cat-1");
        assertThat(children.orElseThrow().children())
                .extracting(CategoryView::id)
                .containsExactlyInAnyOrder("cat-2", "cat-3");
    }

    @Test
    void findParent_returnsParentWithGrandparentId() {
        CategoryEntity accessories = category("cat-4", "Accessories");
        categoryNodeRepository.insert(accessories, "cat-2");
        entityManager.flush();
        entityManager.clear();

        Optional<CategoryView> parent = categoryQueryRepository.findParent("cat-4");

        assertThat(parent).isPresent();
        assertThat(parent.orElseThrow().id()).isEqualTo("cat-2");
        assertThat(parent.orElseThrow().parentId()).isEqualTo("cat-1");
    }

    private CategoryEntity category(String uuid, String name) {
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
