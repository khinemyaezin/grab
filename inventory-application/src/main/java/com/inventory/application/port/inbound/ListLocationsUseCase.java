package com.inventory.application.port.inbound;

import com.inventory.application.model.read.ListLocationsQuery;
import org.springframework.data.domain.Page;
import com.inventory.application.model.read.ListLocationsResult;

public interface ListLocationsUseCase {
    Page<ListLocationsResult> execute(ListLocationsQuery query);
}
