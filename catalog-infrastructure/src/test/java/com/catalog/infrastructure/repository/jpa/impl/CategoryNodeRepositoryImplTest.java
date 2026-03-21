package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.exception.CatalogInfraError;
import com.catalog.infrastructure.exception.CatalogInfraException;
import com.catalog.infrastructure.repository.jpa.*;
import com.catalog.infrastructure.view.CategoryTreeNode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryNodeRepositoryImplTest extends CategoryRepositoryTestConfig  {

    @Autowired
    private CategoryNodeRepository categoryNodeRepository;

    @Autowired
    private CategoryJpaRepo categoryJpaRepo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void insert_withNullOrBlankParent_routesToRootInsertion() {
        insert("root-1", "Root One", null);
        flushAndClear();

        assertThat(categoryNodeRepository.findParent("root-1")).isEmpty();
        assertThat(categoryJpaRepo.findByUuid("root-1")).hasValueSatisfying(root -> assertThat(root.getDepth()).isZero());

        assertThatThrownBy(() -> insert("root-2", "Root Two", "   "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Root node already exists");
    }

    @Test
    void insert_withExistingParent_insertsAsLastChild() {
        insert("root", "Electronics", null);
        insert("phones", "Phones", "root");
        flushAndClear();

        assertThat(categoryNodeRepository.findParent("phones"))
                .map(CategoryEntity::getUuid)
                .contains("root");
        assertThat(categoryNodeRepository.findImmediateChildren("root"))
                .extracting(CategoryEntity::getUuid)
                .containsExactly("phones");
    }

    @Test
    void insert_withMissingParent_throwsNotFoundError() {
        assertThatThrownBy(() -> insert("orphan", "Orphan", "missing-parent"))
                .isInstanceOfSatisfying(CatalogInfraException.class, exception -> {
                    assertThat(exception.getMessage()).isEqualTo("Parent category not found: missing-parent.");
                    assertThat(exception.getMessageSource()).isInstanceOf(CatalogInfraError.PersistenceNotFound.class);
                    CatalogInfraError.PersistenceNotFound error =
                            (CatalogInfraError.PersistenceNotFound) exception.getMessageSource();
                    assertThat(error.resource()).isEqualTo("Category");
                    assertThat(error.id()).isEqualTo("missing-parent");
                });

        assertThat(categoryJpaRepo.findByUuid("orphan")).isEmpty();
    }

    @Test
    void findParent_overloads_returnExpectedParent() {
        seedElectronicsTree();
        flushAndClear();

        CategoryEntity android = categoryJpaRepo.findByUuid("android").orElseThrow();

        assertThat(categoryNodeRepository.findParent(android))
                .map(CategoryEntity::getUuid)
                .contains("phones");
        assertThat(categoryNodeRepository.findParent("android"))
                .map(CategoryEntity::getUuid)
                .contains("phones");
        assertThat(categoryNodeRepository.findParent("root")).isEmpty();
        assertThat(categoryNodeRepository.findParent("missing")).isEmpty();
    }

    @Test
    void findSubtree_returnsHierarchyWithParentIds() {
        seedElectronicsTree();
        flushAndClear();

        CategoryTreeNode rootTree = categoryNodeRepository.findSubtree("root").orElseThrow();

        assertThat(rootTree.entity().getUuid()).isEqualTo("root");
        assertThat(rootTree.parentId()).isNull();
        assertThat(rootTree.children())
                .extracting(node -> node.entity().getUuid())
                .containsExactlyInAnyOrder("phones", "laptops");

        CategoryTreeNode phones = rootTree.children().stream()
                .filter(node -> "phones".equals(node.entity().getUuid()))
                .findFirst()
                .orElseThrow();
        assertThat(phones.parentId()).isEqualTo("root");
        assertThat(phones.children())
                .extracting(node -> node.entity().getUuid())
                .containsExactlyInAnyOrder("android", "ios");
        assertThat(phones.children())
                .extracting(CategoryTreeNode::parentId)
                .containsOnly("phones");
    }

    @Test
    void findImmediateChildren_returnsOnlyDirectChildren_andEmptyWhenMissing() {
        seedElectronicsTree();
        flushAndClear();

        assertThat(categoryNodeRepository.findImmediateChildren("root"))
                .extracting(CategoryEntity::getUuid)
                .containsExactlyInAnyOrder("phones", "laptops");
        assertThat(categoryNodeRepository.findImmediateChildren("phones"))
                .extracting(CategoryEntity::getUuid)
                .containsExactlyInAnyOrder("android", "ios");
        assertThat(categoryNodeRepository.findImmediateChildren("missing")).isEmpty();
    }

    @Test
    void findLeafNodeByName_returnsLeavesFromMatchedSubtree() {
        seedElectronicsTree();
        flushAndClear();

        assertThat(categoryNodeRepository.findLeafNodeByName("Pho"))
                .extracting(CategoryEntity::getUuid)
                .containsExactly("android", "ios");
        assertThat(categoryNodeRepository.findLeafNodeByName("Ele"))
                .extracting(CategoryEntity::getUuid)
                .containsExactly("android", "ios", "laptops");
    }

    @Test
    void findSubtreeIds_returnsPreOrderIds_andEmptyWhenMissing() {
        seedElectronicsTree();
        flushAndClear();

        assertThat(categoryNodeRepository.findSubtreeIds("root"))
                .containsExactlyInAnyOrder("root", "phones", "android", "ios", "laptops");
        assertThat(categoryNodeRepository.findSubtreeIds("missing")).isEmpty();
    }

    @Test
    void removeSubtree_removesOnlyTargetSubtree_andIgnoresMissing() {
        seedElectronicsTree();
        flushAndClear();

        categoryNodeRepository.removeSubtree("phones");
        categoryNodeRepository.removeSubtree("missing");
        flushAndClear();

        assertThat(categoryJpaRepo.findByUuid("phones")).isEmpty();
        assertThat(categoryJpaRepo.findByUuid("android")).isEmpty();
        assertThat(categoryJpaRepo.findByUuid("ios")).isEmpty();

        assertThat(categoryJpaRepo.findByUuid("root")).isPresent();
        assertThat(categoryJpaRepo.findByUuid("laptops")).isPresent();
    }

    private void seedElectronicsTree() {
        insert("root", "Electronics", null);
        insert("phones", "Phones", "root");
        insert("android", "Android", "phones");
        insert("ios", "iOS", "phones");
        insert("laptops", "Laptops", "root");
    }

    private void insert(String uuid, String name, String parentUuid) {
        CategoryEntity entity = new CategoryEntity();
        entity.setUuid(uuid);
        entity.setName(name);
        entity.setActive(true);
        entity.setListingAllowed(true);
        entity.setReviewRequired(false);
        entity.setC2cAllowed(true);
        categoryNodeRepository.insert(entity, parentUuid);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

}
