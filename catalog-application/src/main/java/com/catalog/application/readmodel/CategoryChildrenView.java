package com.catalog.application.readmodel;

import java.util.List;

public record CategoryChildrenView(
        String parentId,
        List<CategoryView> children
) {
}
