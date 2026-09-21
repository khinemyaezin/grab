package com.inventory.application.port.inbound;

import com.inventory.application.model.read.CheckInventoryExistenceQuery;
import com.inventory.application.model.read.CheckInventoryExistenceResult;

public interface CheckInventoryExistenceUseCase {
    CheckInventoryExistenceResult execute(CheckInventoryExistenceQuery query);
}
