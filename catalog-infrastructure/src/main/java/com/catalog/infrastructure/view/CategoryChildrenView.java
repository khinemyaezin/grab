package com.catalog.infrastructure.view;

import java.util.List;

public record CategoryChildrenView(
        String parentId,
        List<CategoryView> children
) {
}
