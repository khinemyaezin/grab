package com.catalog.application.model.read;

import java.util.List;

public record CategoryChildrenResult(
        String parentId,
        List<CategoryResult> children
) {
}
