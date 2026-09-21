package com.inventory.application.port.inbound;

import com.inventory.application.model.read.SearchInventoryQuery;
import org.springframework.data.domain.Page;
import com.inventory.application.model.read.SearchInventoryResult;

public interface SearchInventoryUseCase {
    Page<SearchInventoryResult> execute(SearchInventoryQuery query);
}
