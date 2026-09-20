package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetBinLocationIdQuery;

public interface GetBinLocationIdUseCase {
    String execute(GetBinLocationIdQuery query);
}
