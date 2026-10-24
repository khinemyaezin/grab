package com.catalog.application.model.read;

import java.util.List;

public record CategoryNodeView(
        String id,
        String name,
        String parentId,
        List<CategoryNodeView> children
) {
    public CategoryNodeView {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
