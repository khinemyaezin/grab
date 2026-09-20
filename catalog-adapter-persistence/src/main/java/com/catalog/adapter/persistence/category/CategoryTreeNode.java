package com.catalog.adapter.persistence.category;

import com.catalog.adapter.persistence.entity.CategoryEntity;

import java.util.List;

public record CategoryTreeNode(
        CategoryEntity entity,
        String parentId,
        List<CategoryTreeNode> children
) {
}
