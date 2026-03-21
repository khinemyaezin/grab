package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryTreeNode;
import com.catalog.infrastructure.repository.jpa.CategoryNodeRepository;
import com.catalog.infrastructure.view.CategoryChildrenView;
import com.catalog.infrastructure.view.CategoryNodeView;
import com.catalog.infrastructure.view.CategoryView;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CategoryQueryRepositoryImpl implements CategoryQueryRepository {

    private final CategoryJpaRepo categoryJpaRepo;
    private final CategoryNodeRepository categoryNodeRepository;

    @Override
    public boolean exists(String categoryId) {
        return categoryJpaRepo.findByUuid(categoryId).isPresent();
    }

    @Override
    public Optional<CategoryNodeView> findTree(String categoryId) {
        return categoryNodeRepository.findSubtree(categoryId)
                .map(this::toNodeView);
    }

    @Override
    public Optional<CategoryChildrenView> findChildren(String categoryId) {
        return categoryJpaRepo.findByUuid(categoryId)
                .map(category -> new CategoryChildrenView(
                        category.getUuid(),
                        categoryNodeRepository.findImmediateChildren(categoryId).stream()
                                .map(child -> toCategoryView(child, category.getUuid()))
                                .toList()
                ));
    }

    @Override
    public Optional<CategoryView> findParent(String categoryId) {
        return categoryNodeRepository.findParent(categoryId)
                .map(parent -> toCategoryView(
                        parent,
                        categoryNodeRepository.findParent(parent)
                                .map(CategoryEntity::getUuid)
                                .orElse(null)
                ));
    }

    @Override
    public List<CategoryView> findLeafNodesByName(String name) {
        return categoryNodeRepository.findLeafNodeByName(name)
                .stream()
                .map(leaf -> toCategoryView(
                        leaf,
                        categoryNodeRepository.findParent(leaf)
                                .map(CategoryEntity::getUuid)
                                .orElse(null)
                ))
                .toList();
    }

    private CategoryNodeView toNodeView(CategoryTreeNode node) {
        return new CategoryNodeView(
                node.entity().getUuid(),
                node.entity().getName(),
                node.parentId(),
                node.children().stream()
                        .map(this::toNodeView)
                        .toList()
        );
    }

    private CategoryView toCategoryView(CategoryEntity entity, String parentId) {
        return new CategoryView(
                entity.getUuid(),
                entity.getName(),
                parentId,
                Boolean.TRUE.equals(entity.getActive()),
                Boolean.TRUE.equals(entity.getListingAllowed()),
                Boolean.TRUE.equals(entity.getReviewRequired()),
                Boolean.TRUE.equals(entity.getC2cAllowed())
        );
    }
}
