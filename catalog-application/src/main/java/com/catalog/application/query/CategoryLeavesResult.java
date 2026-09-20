package com.catalog.application.query;

import java.util.List;

public record CategoryLeavesResult(
        List<CategoryResult> leaves
) {
}
