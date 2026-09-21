package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.repository.CategoryJpaRepo;
import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.adapter.persistence.category.CategoryTreeNode;
import com.catalog.adapter.persistence.repository.CategoryNodeRepository;
import com.catalog.application.model.read.CategoryChildrenView;
import com.catalog.application.model.read.CategoryNodeView;
import com.catalog.application.model.read.CategoryView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CategoryQueryAdapter implements CategoryQueryPort {

    private static final Logger log = Loggers.getLogger(CategoryQueryAdapter.class);

    private final CategoryJpaRepo categoryJpaRepo;
    private final CategoryNodeRepository categoryNodeRepository;
    private final PersistenceExecutor executor;

    @Override
    public boolean exists(String categoryId) {
        log.debug("Checking category existence for id={}", categoryId);
        return executor.query("Category", () -> categoryJpaRepo.findByUuid(categoryId).isPresent());
    }

    @Override
    public Optional<CategoryNodeView> findTree(String categoryId) {
        log.debug("Loading category tree for id={}", categoryId);
        return executor.query("Category", () -> categoryNodeRepository.findSubtree(categoryId)
                .map(this::toNodeView));
    }

    @Override
    public Optional<CategoryChildrenView> findChildren(String categoryId) {
        log.debug("Loading category children for id={}", categoryId);
        return executor.query("Category", () -> categoryJpaRepo.findByUuid(categoryId)
                .map(category -> new CategoryChildrenView(
                        category.getUuid(),
                        categoryNodeRepository.findImmediateChildren(categoryId).stream()
                                .map(child -> toCategoryView(child, category.getUuid()))
                                .toList()
                )));
    }

    @Override
    public Optional<CategoryView> findParent(String categoryId) {
        log.debug("Loading category parent for id={}", categoryId);
        return executor.query("Category", () -> categoryNodeRepository.findParent(categoryId)
                .map(parent -> toCategoryView(
                        parent,
                        categoryNodeRepository.findParent(parent)
                                .map(CategoryEntity::getUuid)
                                .orElse(null)
                )));
    }

    @Override
    public List<CategoryView> findLeafNodesByName(String name) {
        log.debug("Searching leaf nodes by name={}", name);
        return executor.query("Category", () -> categoryNodeRepository.findLeafNodeByName(name)
                .stream()
                .map(leaf -> toCategoryView(
                        leaf,
                        categoryNodeRepository.findParent(leaf)
                                .map(CategoryEntity::getUuid)
                                .orElse(null)
                ))
                .toList());
    }

    @Override
    public List<CategoryView> findViewByIds(List<String> ids) {
        log.debug("Searching nodes by ids={}", ids);
        return executor.query("Category", ()->
            categoryJpaRepo.findAllByUuids(ids));
    }

    @Override
    public List<CategoryNodeView> findRootTrees() {
        log.debug("Loading root category trees");
        return executor.query("Category", () ->
                categoryJpaRepo.findByDepthOrderByLftAsc(0).stream()
                        .map(root -> categoryNodeRepository.findSubtree(root.getUuid()))
                        .flatMap(Optional::stream)
                        .map(this::toNodeView)
                        .toList());
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
                Boolean.TRUE.equals(entity.getC2cAllowed())
        );
    }
}
