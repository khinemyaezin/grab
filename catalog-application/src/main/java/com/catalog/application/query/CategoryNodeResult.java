package com.catalog.application.query;

import java.util.List;

public record CategoryNodeResult(
        String id,
        String name,
        String parentId,
        List<CategoryNodeResult> children
) {
}
