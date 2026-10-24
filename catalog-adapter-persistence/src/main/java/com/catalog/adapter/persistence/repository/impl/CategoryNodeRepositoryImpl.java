package com.catalog.adapter.persistence.repository.impl;

import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.exception.CatalogInfraError;
import com.catalog.adapter.persistence.exception.CatalogInfraException;
import com.catalog.adapter.persistence.repository.CategoryJpaRepo;
import com.catalog.adapter.persistence.repository.CategoryNestedSetNodeRepository;
import com.catalog.application.model.read.CategoryNodeView;
import com.catalog.adapter.persistence.repository.CategoryNodeRepository;
import com.nestedset.library.model.NodeComponent;
import lombok.AllArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
public class CategoryNodeRepositoryImpl implements CategoryNodeRepository  {
    private final CategoryJpaRepo categoryJpaRepo;
    private final CategoryNestedSetNodeRepository categoryNestedSetNodeRepository;

    @Override
    public void insert(CategoryEntity entity, String parentUuid) {
        if (parentUuid == null || parentUuid.isBlank()) {
            categoryNestedSetNodeRepository.insertAsFirstRoot(entity);
            return;
        }

        CategoryEntity parent = categoryJpaRepo.findByUuid(parentUuid)
                .orElseThrow(() -> new CatalogInfraException(
                        new CatalogInfraError.PersistenceNotFound("Category", parentUuid),
                        "Parent category not found: " + parentUuid + "."
                ));
        categoryNestedSetNodeRepository.insertAsLastChildOf(entity, parent);
    }

    @Override
    public Optional<CategoryEntity> findParent(CategoryEntity categoryEntity) {
        return categoryNestedSetNodeRepository.getParent(categoryEntity);
    }

    @Override
    public Optional<CategoryEntity> findParent(String categoryUuid) {
        return categoryJpaRepo.findByUuid(categoryUuid)
                .flatMap(categoryNestedSetNodeRepository::getParent);
    }

    @Override
    public Optional<CategoryNodeView> findSubtree(String categoryUuid) {
        return categoryJpaRepo.findByUuid(categoryUuid)
                .map(categoryNestedSetNodeRepository::getTree)
                .map(this::toTreeNode);
    }

    @Override
    public List<CategoryEntity> findImmediateChildren(String categoryUuid) {
        return categoryJpaRepo.findByUuid(categoryUuid)
                .map(categoryNestedSetNodeRepository::getImmediateChildren)
                .map(this::safeChildren)
                .orElseGet(Set::of)
                .stream()
                .map(NodeComponent::getNode)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<CategoryEntity> findLeafNodeByName(String name) {
        return categoryNestedSetNodeRepository.findLeafNodeBy(name);
    }

    @Override
    public Set<String> findSubtreeIds(String categoryUuid) {
        return findSubtree(categoryUuid)
                .map(this::collectSubtreeIds)
                .orElseGet(Set::of);
    }

    @Override
    public void removeSubtree(String categoryUuid) {
        categoryJpaRepo.findByUuid(categoryUuid)
                .ifPresent(categoryNestedSetNodeRepository::removeSubtree);
    }

    private CategoryNodeView toTreeNode(NodeComponent<CategoryEntity> node) {
        if (node == null || node.getNode() == null) {
            return null;
        }

        String parentId = node.getParent() != null && node.getParent().getNode() != null
                ? node.getParent().getNode().getUuid()
                : null;

        List<CategoryNodeView> children = safeChildren(node).stream()
                .map(this::toTreeNode)
                .filter(Objects::nonNull)
                .toList();

        CategoryEntity entity = node.getNode();
        return new CategoryNodeView(entity.getUuid(), entity.getName(), parentId, children);
    }

    private Set<String> collectSubtreeIds(CategoryNodeView node) {
        Set<String> categoryIds = new LinkedHashSet<>();
        collectSubtreeIds(node, categoryIds);
        return categoryIds;
    }

    private void collectSubtreeIds(CategoryNodeView node, Set<String> categoryIds) {
        if (node == null || node.id() == null) {
            return;
        }

        categoryIds.add(node.id());
        for (CategoryNodeView child : node.children()) {
            collectSubtreeIds(child, categoryIds);
        }
    }

    private Set<NodeComponent<CategoryEntity>> safeChildren(NodeComponent<CategoryEntity> node) {
        try {
            return node.getChildren();
        } catch (UnsupportedOperationException ignored) {
            return Set.of();
        }
    }
}
