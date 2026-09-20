package com.inventory.application.port.inbound;

import com.inventory.application.model.read.ListBinsByZoneQuery;
import org.springframework.data.domain.Page;
import com.inventory.application.model.read.ListBinsResult;

public interface ListBinsByZoneUseCase {
    Page<ListBinsResult> execute(ListBinsByZoneQuery query);
}
