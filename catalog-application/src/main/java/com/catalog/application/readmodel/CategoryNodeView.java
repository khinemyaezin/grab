package com.catalog.application.readmodel;

import java.util.List;

public record CategoryNodeView(
        String id,
        String name,
        String parentId,
        List<CategoryNodeView> children
) {
}
