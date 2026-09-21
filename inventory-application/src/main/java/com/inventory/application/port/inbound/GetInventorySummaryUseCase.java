package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetInventorySummaryQuery;
import com.inventory.application.model.read.GetInventorySummaryResult;

public interface GetInventorySummaryUseCase {
    GetInventorySummaryResult execute(GetInventorySummaryQuery query);
}
