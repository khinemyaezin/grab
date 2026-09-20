package com.catalog.application.model.read;

import java.util.List;

public record CategoryChildrenView(
        String parentId,
        List<CategoryView> children
) {
}
