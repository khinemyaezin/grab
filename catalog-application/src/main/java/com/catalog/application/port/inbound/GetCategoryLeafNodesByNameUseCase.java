package com.catalog.application.port.inbound;

import com.catalog.application.model.read.CategoryLeavesResult;
import com.catalog.application.model.read.GetCategoryLeafNodesByNameQuery;

public interface GetCategoryLeafNodesByNameUseCase {
    CategoryLeavesResult execute(GetCategoryLeafNodesByNameQuery query);
}
