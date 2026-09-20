package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetInventoryMovementsQuery;
import org.springframework.data.domain.Page;
import com.inventory.application.model.read.GetInventoryMovementsResult;

public interface GetInventoryMovementsUseCase {
    Page<GetInventoryMovementsResult> execute(GetInventoryMovementsQuery query);
}
