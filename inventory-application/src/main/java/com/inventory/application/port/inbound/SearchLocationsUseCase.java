package com.inventory.application.port.inbound;

import com.inventory.application.model.read.SearchLocationsQuery;
import org.springframework.data.domain.Page;
import com.inventory.application.model.read.SearchLocationsResult;

public interface SearchLocationsUseCase {
    Page<SearchLocationsResult> execute(SearchLocationsQuery query);
}
