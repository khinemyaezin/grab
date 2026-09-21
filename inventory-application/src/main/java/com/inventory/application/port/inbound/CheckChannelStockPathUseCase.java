package com.inventory.application.port.inbound;

import com.inventory.application.model.read.CheckChannelStockPathQuery;
import com.inventory.application.model.read.CheckChannelStockPathResult;

public interface CheckChannelStockPathUseCase {
    CheckChannelStockPathResult execute(CheckChannelStockPathQuery query);
}
