package com.catalog.application.query;

import java.util.List;

public record CategoryChildrenResult(
        String parentId,
        List<CategoryResult> children
) {
}
