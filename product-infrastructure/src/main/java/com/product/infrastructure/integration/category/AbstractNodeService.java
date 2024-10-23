package com.product.infrastructure.integration.category;

import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.repository.category.CategoryEntityRepository;

import java.util.List;
import java.util.Optional;

public abstract class AbstractNodeService implements NodeService<CategoryEntity, Long> {
    private final CategoryEntityRepository nodeRepository;

    protected AbstractNodeService(CategoryEntityRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public CategoryEntity createNode(CategoryEntity entity) {
        Integer right = nodeRepository.getMaxRight();
        if (right == null) {
            right = 0;
        }
        right++;

        entity.setLft(right);
        entity.setRgt(right + 1);
        entity.setDepth(0);

        return nodeRepository.save(entity);
    }

    @Override
    public CategoryEntity updateNode(Long Long, CategoryEntity entity) {
        entity = nodeRepository.findById(Long).orElseThrow(() -> new RuntimeException("Node is not found"));
        return nodeRepository.save(entity);
    }

    @Override
    public Optional<CategoryEntity> readNode(Long Long) {
        return nodeRepository.findById(Long);
    }

    @Override
    public CategoryEntity createNode(CategoryEntity entity, Long parentId) {
        CategoryEntity rootNode = nodeRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Integer right = rootNode.getRgt();

        nodeRepository.incrementLeftBoundaryAfter(right);
        nodeRepository.incrementRightBoundaryAfter(right);

        entity.setLft(right);
        entity.setRgt(right + 1);
        entity.setDepth(rootNode.getDepth() + 1);

        return nodeRepository.save(entity);
    }

    @Override
    public void deleteNode(Long Long) {
        CategoryEntity category = nodeRepository.findById(Long).orElseThrow(() -> new RuntimeException("Node not found"));
        Integer left = category.getLft();
        Integer right = category.getRgt();
        Integer width = right - left + 1;

        nodeRepository.removeNodesInRange(left, right);

        nodeRepository.decrementRightBoundaryAfter(right, width);
        nodeRepository.decrementLeftBoundaryAfter(right, width);
    }

    @Override
    public List<CategoryEntity> findImmediateChildren(Long nodeId) {
        return this.nodeRepository.getImmediateChildren(nodeId);
    }

    @Override
    public List<CategoryEntity> findParentOf(Long Long) {
        return this.nodeRepository.getParentOf(Long);
    }
}