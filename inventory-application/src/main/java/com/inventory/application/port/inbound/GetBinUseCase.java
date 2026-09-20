package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetBinQuery;
import com.inventory.application.model.read.GetBinResult;

public interface GetBinUseCase {
    GetBinResult execute(GetBinQuery query);
}
