package com.inventory.application.port.inbound;

import com.inventory.application.model.read.SearchZonesQuery;
import org.springframework.data.domain.Page;
import com.inventory.application.model.read.SearchZonesResult;

public interface SearchZonesUseCase {
    Page<SearchZonesResult> execute(SearchZonesQuery query);
}
