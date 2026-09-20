package com.catalog.application.model.read;

import java.util.List;

public record CategoryNodeResult(
        String id,
        String name,
        String parentId,
        List<CategoryNodeResult> children
) {
}
