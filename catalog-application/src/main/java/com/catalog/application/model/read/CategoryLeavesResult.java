package com.catalog.application.model.read;

import java.util.List;

public record CategoryLeavesResult(
        List<CategoryResult> leaves
) {
}
