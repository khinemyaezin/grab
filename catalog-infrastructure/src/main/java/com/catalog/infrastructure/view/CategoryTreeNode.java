package com.catalog.infrastructure.view;

import com.catalog.infrastructure.entity.entity.CategoryEntity;

import java.util.List;

public record CategoryTreeNode(
        CategoryEntity entity,
        String parentId,
        List<CategoryTreeNode> children
) {
}
