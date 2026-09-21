package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.read.SalesChannelResult;

public interface GetSalesChannelUseCase {
    SalesChannelResult execute(GetSalesChannelQuery query);
}
